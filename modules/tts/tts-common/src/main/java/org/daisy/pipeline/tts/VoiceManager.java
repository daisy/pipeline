package org.daisy.pipeline.tts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.LanguageRange;
import static org.daisy.pipeline.tts.VoiceInfo.NO_DEFINITE_GENDER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceManager {

	private Logger ServerLogger = LoggerFactory.getLogger(VoiceManager.class);

	/**
	 * Map of the best services for each available voice, given that two different services can serve the same voice
	 */
	private final Map<Voice,TTSEngine> bestEngines;
	/**
	 * Map from voice name/engine to voice
	 */
	private final Map<VoiceKey,Voice> primaryVoices;
	/**
	 * Map from voice (name/engine) to a collection of fallback voices (sorted by matching engine,
	 * matching gender and priority).
	 */
	private final Map<VoiceKey,Collection<Voice>> secondaryVoices;
	/**
	 * Map from voice properties (language/gender/engine) to voices. Each combination of primary
	 * language, gender and engine is mapped to a collection of voices with corresponding language
	 * range, sorted by priority.
	 */
	private final Map<VoiceKey,Collection<Entry<Voice,Collection<LanguageRange>>>> voiceIndex;

	public VoiceManager(Collection<TTSEngine> engines, Collection<VoiceInfo> voiceInfoFromConfig) {

		// create a map of the best services for each available voice
		bestEngines = new LinkedHashMap<Voice,TTSEngine>(); { // LinkedHashMap: iteration order = insertion order
			TTSTimeout timeout = new TTSTimeout();
			int timeoutSecs = 30;
			// sort engines by engine priority, so that voice info from engines (see below) is sorted by engine priority
			// as well
			List<TTSEngine> sortedEngines = new ArrayList<>(engines);
			Collections.sort(sortedEngines, Comparator.comparingInt(TTSEngine::getOverallPriority).reversed());
			for (TTSEngine tts : sortedEngines) {
				timeout.enableForCurrentThread(timeoutSecs);
				try {
					Collection<Voice> voices = tts.getAvailableVoices();
					if (voices != null)
						for (Voice v : voices)
							if (!bestEngines.containsKey(v))
								bestEngines.put(v, tts);
				} catch (SynthesisException e) {
					ServerLogger.error("error while retrieving the voices of "
					                   + tts.getProvider().getName());
					ServerLogger.debug(tts.getProvider().getName()
					                   + " getAvailableVoices error: " + getStack(e));
				} catch (InterruptedException e) {
					ServerLogger.error("timeout while retrieving the voices of "
					                   + tts.getProvider().getName()
					                   + " (exceeded " + timeoutSecs + " seconds)");
				} finally {
					timeout.disable();
				}
			}
			timeout.close();
		}

		// get mappings from voice properties (language/gender) to voice (name/engine)
		List<VoiceInfo> voiceInfo = new ArrayList<>();

		// get info from engines (lowest priority)
		primaryVoices = new HashMap<>();
		for (Voice v : bestEngines.keySet()) {
			primaryVoices.put(new VoiceKey(v.getEngine(), v.getName()), v);
			if (!v.getLocale().isEmpty() && v.getGender().isPresent()) {
				Gender g = v.getGender().get();
				for (LanguageRange l : v.getLocale())
					voiceInfo.add(new VoiceInfo(v.getEngine(), v.getName(), l, g, 0));
			}
		}

		// voices also match less specific locales (without region tag)
		final float priorityVariantPenalty = 0.1f;
		List<VoiceInfo> derivedVoiceInfo = new ArrayList<>(); {
			for (VoiceInfo vi : voiceInfo) {
				Locale shortLang = vi.language.getPrimaryLanguageSubTag();
				if (shortLang != null && !shortLang.toString().equals(vi.language.toString()))
					derivedVoiceInfo.add(
						new VoiceInfo(vi.voiceEngine, vi.voiceName, shortLang, vi.gender,
						              vi.priority - priorityVariantPenalty));
			}
		}
		voiceInfo.addAll(derivedVoiceInfo);

		// get info from configuration
		// the configuration is interpreted as-is. region and other subtags are significant
		// due to fuzzy voice selection, a voice can be applied to less specific locales, but it is
		// not an exact match
		voiceInfo.addAll(voiceInfoFromConfig);

		// sort by priority and language range specificity (descending)
		// voices with equal priority and specificity will not be reordered as a result of the sort
		Collections.sort(voiceInfo, sortByPriority.thenComparing(sortBySpecificity));

		// filter available voices
		List<VoiceInfo> availableVoiceInfo = new ArrayList<>(); {
			for (VoiceInfo i : voiceInfo)
				if (primaryVoices.containsKey(new VoiceKey(i.voiceEngine, i.voiceName)))
					availableVoiceInfo.add(i);
		}

		// create map of the best fallback for each voice
		// engine is more important than gender and priority, gender is more important than priority
		secondaryVoices = new HashMap<>(); {
			for (boolean sameEngine : new boolean[]{true, false})
				for (boolean sameGender : new boolean[]{true, false})
					for (VoiceInfo best : voiceInfo) {
						VoiceKey bestKey = new VoiceKey(best.voiceEngine, best.voiceName);
						for (VoiceInfo fallback : availableVoiceInfo) {
							VoiceKey fallbackKey = new VoiceKey(fallback.voiceEngine, fallback.voiceName);
							if (!fallbackKey.equals(bestKey))
								if (best.language.getPrimaryLanguageSubTag() == null) {
									if (!sameGender && !sameEngine) {
										// multilang fallback voices are only considered when gender
										// and engine are not criteria so as to prevent the algo
										// from choosing a multilang voice with the same engine over
										// a regular voice with a different engine.
										Collection<Voice> vv = secondaryVoices.get(bestKey);
										if (vv == null) {
											vv = new LinkedHashSet<>();
											secondaryVoices.put(bestKey, vv);
										}
										vv.add(primaryVoices.get(fallbackKey));
									}
								} else if (fallback.language.equals(best.language)
								           && (!sameGender
								               // fallback voices with unknown gender are only considered
								               // when gender is not a criteria
								               || (fallback.gender != NO_DEFINITE_GENDER
								                   && fallback.gender == best.gender))
								           && (!sameEngine
								               || fallback.voiceEngine.equals(best.voiceEngine))) {
									Collection<Voice> vv = secondaryVoices.get(bestKey);
									if (vv == null) {
										vv = new LinkedHashSet<>();
										secondaryVoices.put(bestKey, vv);
									}
									vv.add(primaryVoices.get(fallbackKey));
								}
						}
					}
		}

		// create index of voices with language, engine and gender as keys
		Set<Locale> allLangs = new HashSet<Locale>(); {
			for (VoiceInfo vi : availableVoiceInfo) {
				Locale shortLang = vi.language.getPrimaryLanguageSubTag();
				if (shortLang != null)
					allLangs.add(shortLang); }}
		Set<Gender> allGenders = new HashSet<Gender>(); {
			for (VoiceInfo vi : availableVoiceInfo) allGenders.add(vi.gender);
			allGenders.remove(NO_DEFINITE_GENDER); }
		Map<VoiceKey,LinkedHashMap<Voice,Collection<LanguageRange>>> voiceIndex = new HashMap<>(); {
			for (VoiceInfo vi : availableVoiceInfo) {
				if (vi.language.getPrimaryLanguageSubTag() == null)
					// this is to make sure that a multi-lang voice wins from a regular voice if it has
					// a higher priority
					for (Locale shortLang : allLangs)
						for (boolean sameEngine : new boolean[]{true, false}) {
							if (vi.gender == NO_DEFINITE_GENDER)
								// this is to make sure that a voice with unknown gender wins from a regular voice
								// if it has a higher priority
								for (Gender g : allGenders) {
									VoiceKey k = new VoiceKey(shortLang, g, sameEngine ? vi.voiceEngine : null);
									LinkedHashMap<Voice,Collection<LanguageRange>> vv = voiceIndex.get(k);
									if (vv == null) {
										vv = new LinkedHashMap<>();
										voiceIndex.put(k, vv);
									}
									Voice v = primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName));
									Collection<LanguageRange> lang = vv.get(v);
									if (lang == null) {
										lang = new HashSet<>();
										vv.put(v, lang);
									}
									lang.add(vi.language);
								}
							for (boolean sameGender : new boolean[]{true, false}) {
								VoiceKey k = new VoiceKey(shortLang,
								                          sameGender ? vi.gender : null,
								                          sameEngine ? vi.voiceEngine : null);
								LinkedHashMap<Voice,Collection<LanguageRange>> vv = voiceIndex.get(k);
								if (vv == null) {
									vv = new LinkedHashMap<>();
									voiceIndex.put(k, vv);
								}
								Voice v = primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName));
								Collection<LanguageRange> lang = vv.get(v);
								if (lang == null) {
									lang = new HashSet<>();
									vv.put(v, lang);
								}
								lang.add(vi.language);
							}
						}
				for (boolean sameLanguage : new boolean[]{true, false}) {
					Locale shortLang = vi.language.getPrimaryLanguageSubTag();
					if (shortLang == null) shortLang = VoiceKey.MUL;
					for (boolean sameEngine : new boolean[]{true, false}) {
						if (vi.gender == NO_DEFINITE_GENDER)
							for (Gender g : allGenders) {
								VoiceKey k = new VoiceKey(sameLanguage ? shortLang : null,
								                          g,
								                          sameEngine ? vi.voiceEngine : null);
								LinkedHashMap<Voice,Collection<LanguageRange>> vv = voiceIndex.get(k);
								if (vv == null) {
									vv = new LinkedHashMap<>();
									voiceIndex.put(k, vv);
								}
								Voice v = primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName));
								Collection<LanguageRange> lang = vv.get(v);
								if (lang == null) {
									lang = new HashSet<>();
									vv.put(v, lang);
								}
								lang.add(vi.language);
							}
						for (boolean sameGender : new boolean[]{true, false}) {
							VoiceKey k = new VoiceKey(sameLanguage ? shortLang : null,
							                          sameGender ? vi.gender : null,
							                          sameEngine ? vi.voiceEngine : null);
							LinkedHashMap<Voice,Collection<LanguageRange>> vv = voiceIndex.get(k);
							if (vv == null) {
								vv = new LinkedHashMap<>();
								voiceIndex.put(k, vv);
							}
							Voice v = primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName));
							Collection<LanguageRange> lang = vv.get(v);
							if (lang == null) {
								lang = new HashSet<>();
								vv.put(v, lang);
							}
							lang.add(vi.language);
						}
					}
				}
			}
		}
		this.voiceIndex = Maps.transformValues(voiceIndex, Map::entrySet);

		// log
		StringBuilder sb = new StringBuilder("Available voices:");
		for (Entry<Voice,TTSEngine> e : bestEngines.entrySet())
			sb.append("\n * ")
			  .append(e.getKey());
		ServerLogger.debug(sb.toString());
		sb = new StringBuilder("Voice selection data:");
		Collection<VoiceInfo> sortedAvailableVoiceInfo = new TreeSet<VoiceInfo>(
			new Comparator<VoiceInfo>() {
				public int compare(VoiceInfo vi1, VoiceInfo vi2) {
					// first group by locale
					// multi-lang voices last
					if (vi1.language.getPrimaryLanguageSubTag() == null && vi2.language.getPrimaryLanguageSubTag() != null)
						return 1;
					else if (vi1.language.getPrimaryLanguageSubTag() != null && vi2.language.getPrimaryLanguageSubTag() == null)
						return -1;
					int compare = vi1.language.toString().compareTo(vi2.language.toString());
					if (compare != 0) return compare;
					// then group by gender
					compare = vi1.gender.compareTo(vi2.gender);
					if (compare != 0) return compare;
					// remove duplicates
					// (Note that the duplicate with the highest priority is kept because availableVoiceInfo
					// is sorted by descending priority and because of the way TreeSet works)
					if (vi1.voiceEngine.equals(vi2.voiceEngine) && vi1.voiceName.equals(vi2.voiceName))
						return 0;
					// highest priority first
					compare = Float.valueOf(vi2.priority).compareTo(Float.valueOf(vi1.priority));
					if (compare != 0)
						return compare;
					compare = vi2.voiceEngine.compareTo(vi1.voiceEngine);
					if (compare != 0)
						return compare;
					return vi2.voiceName.compareTo(vi1.voiceName);
				}
			}
		);
		sortedAvailableVoiceInfo.addAll(availableVoiceInfo);
		for (VoiceInfo vi : sortedAvailableVoiceInfo)
			sb.append("\n * {")
			  .append("locale:").append(vi.language)
			  .append(", gender:").append(vi.gender)
			  .append("}")
			  .append(" -> ")
			  .append(primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName)));
		ServerLogger.debug(sb.toString());
	}

	public Voice findSecondaryVoice(Voice v) {
		Collection<Voice> vv = secondaryVoices.get(new VoiceKey(v.getEngine(), v.getName()));
		if (vv != null)
			return Iterables.getFirst(vv, null);
		return null;
	}

	/**
	 * @param voice is an available voice.
	 * @return the best TTS Engine for @param voice. It can return an engine
	 *         whose OSGi service is no longer enable.
	 */
	public TTSEngine getTTS(Voice voice) {
		return bestEngines.get(voice);
	}

	/**
	 * @param voiceEngine or {@code null} if unknown
	 * @param voiceName   or {@code null} if unknown
	 * @param lang        or {@code null} if unknown
	 * @param gender      or {@code null} if unknown
	 *
	 * @return {@code null} if no voice is available for the given parameters.
	 */
	public Voice findAvailableVoice(String voiceEngine, String voiceName, Locale lang, Gender gender) {
		return Iterables.getFirst(findAvailableVoices(voiceEngine, voiceName, lang, gender), null);
	}

	public Iterable<Voice> findAvailableVoices(String voiceEngine, String voiceName, Locale lang, Gender gender) {
		Set<Voice> voices = new LinkedHashSet<>();
		if (lang == null && gender == null &&
		    voiceEngine != null && !voiceEngine.isEmpty() && voiceName != null && !voiceName.isEmpty()) {
			VoiceKey preferred = new VoiceKey(voiceEngine, voiceName);
			Voice primary = primaryVoices.get(preferred);
			if (primary != null)
				voices.add(primary);
			Collection<Voice> fallback = secondaryVoices.get(preferred);
			if (fallback != null)
				voices.addAll(fallback);
		}
		if (lang != null || gender != null || voiceName == null) {
			Locale shortLang = lang != null ? new Locale(lang.getLanguage()) : lang;

			// engine is more important than gender, region and priority, gender is more important than region
			// and priority, region is more important than priority
			addExactMatches(voices, lang, shortLang, gender, voiceEngine);
			if (gender != null && gender != NO_DEFINITE_GENDER)
				addExactMatches(voices, lang, shortLang, NO_DEFINITE_GENDER, voiceEngine);
			if (lang != null) {
				if (!lang.equals(shortLang)) {
					addExactMatches(voices, shortLang, shortLang, gender, voiceEngine);
					if (gender != null && gender != NO_DEFINITE_GENDER)
						addExactMatches(voices, shortLang, shortLang, NO_DEFINITE_GENDER, voiceEngine);
				}
				if (!lang.equals(VoiceKey.MUL)) {
					addExactMatches(voices, VoiceKey.MUL, VoiceKey.MUL, gender, voiceEngine);
					if (gender != null && gender != NO_DEFINITE_GENDER)
						addExactMatches(voices, VoiceKey.MUL, VoiceKey.MUL, NO_DEFINITE_GENDER, voiceEngine);
				}
			}
			if (gender != null) {
				addExactMatches(voices, lang, shortLang, null, voiceEngine);
				if (lang != null) {
					if (!lang.equals(shortLang))
						addExactMatches(voices, shortLang, shortLang, null, voiceEngine);
					if (!lang.equals(VoiceKey.MUL))
						addExactMatches(voices, VoiceKey.MUL, VoiceKey.MUL, null, voiceEngine);
				}
			}
			if (voiceEngine != null) {
				addExactMatches(voices, lang, shortLang, gender, null);
				if (gender != null && gender != NO_DEFINITE_GENDER)
					addExactMatches(voices, lang, shortLang, NO_DEFINITE_GENDER, null);
				if (lang != null) {
					if (!lang.equals(shortLang)) {
						addExactMatches(voices, shortLang, shortLang, gender, null);
						if (gender != null && gender != NO_DEFINITE_GENDER)
							addExactMatches(voices, shortLang, shortLang, NO_DEFINITE_GENDER, null);
					}
					if (!lang.equals(VoiceKey.MUL)) {
						addExactMatches(voices, VoiceKey.MUL, VoiceKey.MUL, gender, null);
						if (gender != null && gender != NO_DEFINITE_GENDER)
							addExactMatches(voices, VoiceKey.MUL, VoiceKey.MUL, NO_DEFINITE_GENDER, null);
					}
				}
			}
			if (gender != null && voiceEngine != null) {
				addExactMatches(voices, lang, shortLang, null, null);
				if (lang != null) {
					if (!lang.equals(shortLang))
						addExactMatches(voices, shortLang, shortLang, null, null);
					if (!lang.equals(VoiceKey.MUL))
						addExactMatches(voices, VoiceKey.MUL, VoiceKey.MUL, null, null);
				}
			}
		}
		return voices;
	}

	private void addExactMatches(Collection<Voice> collect, Locale lang, Locale shortLang, Gender gender, String voiceEngine) {
		Collection<Entry<Voice,Collection<LanguageRange>>> vv = voiceIndex.get(new VoiceKey(shortLang, gender, voiceEngine));
		if (vv != null)
			for (Entry<Voice,Collection<LanguageRange>> v : vv)
				if (lang == null || LanguageRange.matches(v.getValue(), lang))
					collect.add(v.getKey());
	}

	/**
	 * Whether the given voice matches exactly the given voice engine, voice name, language and/or gender.
	 */
	public boolean matches(Voice voice, String voiceEngine, String voiceName, Locale lang, Gender gender) {
		if (voiceEngine != null && !voiceEngine.equalsIgnoreCase(voice.getEngine()))
			return false;
		if (voiceName != null && !voiceName.equalsIgnoreCase(voice.getName()))
			return false;
		if (lang == null && gender == null)
			return true;
		for (VoiceKey k : voiceIndex.keySet()) {
			if (k.primaryLang != null && k.gender != null && k.engine != null)
				for (Entry<Voice,Collection<LanguageRange>> v : voiceIndex.get(k))
					if (v.getKey().equals(voice)) {
						if (lang != null && !LanguageRange.matches(v.getValue(), lang))
							break;
						else if (gender != null
						    && k.gender != NO_DEFINITE_GENDER
						    && !k.gender.equals(gender))
							break;
						else
							return true;
					}
		}
		return false;
	}

	private static String getStack(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}

	// sort VoiceInfo by priority (descending)
	private static final Comparator<VoiceInfo> sortByPriority = new Comparator<VoiceInfo>() {
			public int compare(VoiceInfo vi1, VoiceInfo vi2) {
				return Float.valueOf(vi2.priority).compareTo(Float.valueOf(vi1.priority));
			}
		};

	// sort VoiceInfo by specificity of language range (descending)
	private static final Comparator<VoiceInfo> sortBySpecificity
		= Comparator.<VoiceInfo>comparingInt(vi -> vi.language.getSpecificity()).reversed();

	private static class VoiceKey {

		private static final Locale MUL = new Locale("mul");

		private final String engine;
		private final String name;
		private final Locale primaryLang; // primary language subtag or "mul"
		private final Gender gender;

		public VoiceKey(String engine, String name) {
			this.engine = engine == null ? null : engine.toLowerCase();
			this.name = name == null ? null : name.toLowerCase();
			this.primaryLang = null;
			this.gender = null;
		}

		public VoiceKey(Locale primaryLang, Gender gender, String engine) {
			this.primaryLang = primaryLang;
			this.gender = gender;
			this.engine = engine == null ? null : engine.toLowerCase();
			this.name = null;
		}

		@Override
		public int hashCode() {
			int res = 0;
			if (this.primaryLang != null)
				res ^=  this.primaryLang.hashCode();
			if (this.gender != null)
				res ^= this.gender.hashCode();
			if (this.engine != null)
				res ^= this.engine.hashCode();
			if (this.name != null)
				res ^= this.name.hashCode();
			return res;
		}

		@Override
		public boolean equals(Object other) {
			VoiceKey o = (VoiceKey) other;
			return (primaryLang == o.primaryLang || (primaryLang != null && primaryLang.equals(o.primaryLang)))
			        && (gender == o.gender || (gender != null && gender.equals(o.gender)))
			        && (engine == o.engine || (engine != null && engine.equals(o.engine)))
			        && (name == o.name || (name != null && name.equals(o.name)));
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("{");
			if (engine != null)
				s.append("engine: " + engine);
			if (name != null)
				s.append(", name: " + name);
			if (primaryLang != null)
				s.append(", primaryLang: " + primaryLang);
			if (gender != null)
				s.append(", gender: " + gender);
			if (s.length() > 1 && s.charAt(1) == ',')
				s.delete(1, 3);
			s.append("}");
			return s.toString();
		}
	}
}
