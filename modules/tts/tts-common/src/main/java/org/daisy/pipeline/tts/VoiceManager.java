package org.daisy.pipeline.tts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import static org.daisy.pipeline.tts.VoiceInfo.NO_DEFINITE_GENDER;
import static org.daisy.pipeline.tts.VoiceInfo.NO_DEFINITE_LANG;

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
	 * Map of the best fallback voice for each voice (name/engine)
	 */
	private final Map<VoiceKey,Voice> secondaryVoices;
	/**
	 * Map from voice properties (language/gender/engine) to voice. Each language/gender/engine
	 * combination is mapped to at most one voice.
	 */
	private final Map<VoiceKey,Voice> voiceIndex;
	/**
	 * Map from voice properties (language/gender) to voices (name/engine). Each language/gender
	 * combination can be mapped to multiple voices.
	 */
	private final List<VoiceInfo> voiceInfo;

	public VoiceManager(Collection<TTSEngine> engines, Collection<VoiceInfo> voiceInfoFromConfig) {

		// create a map of the best services for each available voice
		bestEngines = new HashMap<Voice,TTSEngine>(); {
			TTSTimeout timeout = new TTSTimeout();
			int timeoutSecs = 30;
			for (TTSEngine tts : engines) {
				timeout.enableForCurrentThread(timeoutSecs);
				try {
					Collection<Voice> voices = tts.getAvailableVoices();
					if (voices != null)
						for (Voice v : voices) {
							TTSEngine competitor = bestEngines.get(v);
							if (competitor == null
							    || competitor.getOverallPriority() < tts.getOverallPriority()) {
								bestEngines.put(v, tts);
							}
						}
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
		voiceInfo = new ArrayList<>();

		// get info from configuration
		voiceInfo.addAll(voiceInfoFromConfig);

		// get info from engines (lowest priority)
		primaryVoices = new HashMap<>();
		for (Voice v : bestEngines.keySet()) {
			primaryVoices.put(new VoiceKey(v.getEngine(), v.getName()), v);
			if (v.getLocale().isPresent() && v.getGender().isPresent())
				voiceInfo.add(new VoiceInfo(v.getEngine(),
				                            v.getName(),
				                            v.getLocale().get(),
				                            v.getGender().get(),
				                            0));
		}

		// voices can also be applied to less specific locales (without region tag)
		final float priorityVariantPenalty = 0.1f;
		List<VoiceInfo> derivedVoiceInfo = new ArrayList<>(); {
			for (VoiceInfo vi : voiceInfo)
				if (!vi.isMultiLang()) {
					Locale shortLang = new Locale(vi.language.getLanguage());
					if (!vi.language.equals(shortLang))
						derivedVoiceInfo.add(
							new VoiceInfo(vi.voiceEngine, vi.voiceName, shortLang, vi.gender,
							              vi.priority - priorityVariantPenalty));
				}
		}
		voiceInfo.addAll(derivedVoiceInfo);

		// sort by priority (descending)
		Collections.sort(voiceInfo, new Comparator<VoiceInfo>() {
				public int compare(VoiceInfo vi1, VoiceInfo vi2) {
					return Float.valueOf(vi2.priority).compareTo(Float.valueOf(vi1.priority));
				}});

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
						if (!secondaryVoices.containsKey(bestKey))
							for (VoiceInfo fallback : availableVoiceInfo) {
								VoiceKey fallbackKey = new VoiceKey(fallback.voiceEngine, fallback.voiceName);
								if (!fallbackKey.equals(bestKey))
									if (fallback.isMultiLang()) {
										if (!sameGender && !sameEngine) {
											// multilang fallback voices are only considered when gender
											// and engine are not criteria so as to prevent the algo
											// from choosing a multilang voice with the same engine over
											// a regular voice with a different engine.
											secondaryVoices.put(bestKey, primaryVoices.get(fallbackKey));
											break;
										}
									} else if (fallback.language.equals(best.language)
									           && (!sameGender
									               // fallback voices with unknown gender are only considered
									               // when gender is not a criteria
									               || (fallback.gender != NO_DEFINITE_GENDER
									                   && fallback.gender == best.gender))
									           && (!sameEngine
									               || fallback.voiceEngine.equals(best.voiceEngine))) {
										secondaryVoices.put(bestKey, primaryVoices.get(fallbackKey));
										break;
									}
							}
					}
		}

		// create index of voices with language, engine and gender as keys
		Set<Locale> allLangs = new HashSet<Locale>(); {
			for (VoiceInfo vi : availableVoiceInfo) allLangs.add(vi.language);
			allLangs.remove(NO_DEFINITE_LANG); }
		Set<Gender> allGenders = new HashSet<Gender>(); {
			for (VoiceInfo vi : availableVoiceInfo) allGenders.add(vi.gender);
			allGenders.remove(NO_DEFINITE_GENDER); }
		voiceIndex = new HashMap<VoiceKey,Voice>(); {
			for (VoiceInfo vi : availableVoiceInfo) {
				if (vi.isMultiLang())
					// this is to make sure that a multi-lang voice wins from a regular voice if it has
					// a higher priority
					for (Locale l : allLangs)
						for (boolean sameEngine : new boolean[]{true, false}) {
							if (vi.gender == NO_DEFINITE_GENDER)
								// this is to make sure that a voice with unknown gender wins from a regular voice
								// if it has a higher priority
								for (Gender g : allGenders) {
									VoiceKey k = new VoiceKey(l, g, sameEngine ? vi.voiceEngine : null);
									if (!voiceIndex.containsKey(k))
										voiceIndex.put(k, primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName)));
								}
							for (boolean sameGender : new boolean[]{true, false}) {
								VoiceKey k = new VoiceKey(l,
								                          sameGender ? vi.gender : null,
								                          sameEngine ? vi.voiceEngine : null);
								if (!voiceIndex.containsKey(k))
									voiceIndex.put(k, primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName)));
							}
						}
				for (boolean sameEngine : new boolean[]{true, false}) {
					if (vi.gender == NO_DEFINITE_GENDER)
						for (Gender g : allGenders) {
							VoiceKey k = new VoiceKey(vi.language, g, sameEngine ? vi.voiceEngine : null);
							if (!voiceIndex.containsKey(k))
								voiceIndex.put(k, primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName)));
						}
					for (boolean sameGender : new boolean[]{true, false}) {
						VoiceKey k = new VoiceKey(vi.language,
						                          sameGender ? vi.gender : null,
						                          sameEngine ? vi.voiceEngine : null);
						if (!voiceIndex.containsKey(k))
							voiceIndex.put(k, primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName)));
					}
				}
			}
		}

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
					if (vi1.isMultiLang() && !vi2.isMultiLang()) return 1;
					else if (!vi1.isMultiLang() && vi2.isMultiLang()) return -1;
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
					return Float.valueOf(vi2.priority).compareTo(Float.valueOf(vi1.priority));
				}
			}
		);
		sortedAvailableVoiceInfo.addAll(availableVoiceInfo);
		for (VoiceInfo vi : sortedAvailableVoiceInfo)
			sb.append("\n * {")
			  .append("locale:").append(vi.language == NO_DEFINITE_LANG ? "*" : vi.language)
			  .append(", gender:").append(vi.gender == NO_DEFINITE_GENDER ? "*" : vi.gender)
			  .append("}")
			  .append(" -> ")
			  .append(primaryVoices.get(new VoiceKey(vi.voiceEngine, vi.voiceName)));
		ServerLogger.debug(sb.toString());
	}

	public Voice findSecondaryVoice(Voice v) {
		return secondaryVoices.get(new VoiceKey(v.getEngine(), v.getName()));
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
	 * @return null if no voice is available for the given parameters.
	 * @param voiceEngine is null if unknown
	 * @param voiceName is null if unknown
	 * @param lang is null if unknown
	 * @param gender is null if unknown
	 */
	public Voice findAvailableVoice(String voiceEngine, String voiceName, Locale lang, Gender gender) {

		if (voiceEngine != null && !voiceEngine.isEmpty() && voiceName != null && !voiceName.isEmpty()) {
			VoiceKey preferredVoice = new VoiceKey(voiceEngine, voiceName);
			Voice voice = primaryVoices.get(preferredVoice);
			if (voice != null) {
				return voice;
			} else {
				Voice fallback = secondaryVoices.get(preferredVoice);
				if (fallback != null) {
					return fallback;
				}
			}
		}

		if (lang == null) {
			return null;
		}

		Locale shortLang = new Locale(lang.getLanguage());
		Voice result;

		// engine is more important than gender and priority, gender is more important than priority
		if ((result = voiceIndex.get(new VoiceKey(lang, gender, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(lang, NO_DEFINITE_GENDER, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(shortLang, gender, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(shortLang, NO_DEFINITE_GENDER, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, gender, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, NO_DEFINITE_GENDER, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(lang, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(shortLang, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, voiceEngine))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(lang, gender))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(lang, NO_DEFINITE_GENDER))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(shortLang, gender))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(shortLang, NO_DEFINITE_GENDER))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, gender))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, NO_DEFINITE_GENDER))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(lang))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(shortLang))) != null)
			return result;
		if ((result = voiceIndex.get(new VoiceKey(NO_DEFINITE_LANG))) != null)
			return result;

		return null;
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
		for (VoiceInfo v : voiceInfo) {
			if (v.voiceEngine.equals(voice.getEngine()) && v.voiceName.equals(voice.getName())) {
				if (lang != null
				    && v.language != NO_DEFINITE_LANG
				    && !new Locale(v.language.getLanguage()).equals(new Locale(lang.getLanguage())))
					continue;
				if (gender != null
				    && v.gender != NO_DEFINITE_GENDER
				    && !v.gender.equals(gender))
					continue;
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

	private static class VoiceKey {

		private final String engine;
		private final String name;
		private final Locale lang;
		private final Gender gender;

		public VoiceKey(String engine, String name) {
			this.engine = engine == null ? null : engine.toLowerCase();
			this.name = name == null ? null : name.toLowerCase();
			this.lang = null;
			this.gender = null;
		}

		public VoiceKey(Locale lang) {
			this.lang = lang;
			this.gender = null;
			this.engine = null;
			this.name = null;
		}

		public VoiceKey(Locale lang, Gender gender) {
			this.lang = lang;
			this.gender = gender;
			this.engine = null;
			this.name = null;
		}

		public VoiceKey(Locale lang, String engine) {
			this.lang = lang;
			this.engine = engine == null ? null : engine.toLowerCase();
			this.gender = null;
			this.name = null;
		}

		public VoiceKey(Locale lang, Gender gender, String engine) {
			this.lang = lang;
			this.gender = gender;
			this.engine = engine == null ? null : engine.toLowerCase();
			this.name = null;
		}

		@Override
		public int hashCode() {
			int res = 0;
			if (this.lang != null)
				res ^=  this.lang.hashCode();
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
			return (lang == o.lang || (lang != null && lang.equals(o.lang)))
			        && (gender == o.gender || (gender != null && gender.equals(o.gender)))
			        && (engine == o.engine || (engine != null && engine.equals(o.engine)))
			        && (name == o.name || (name != null && name.equals(o.name)));
		}
	}
}
