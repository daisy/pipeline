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
import java.util.stream.Collectors;

import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import static org.daisy.pipeline.tts.VoiceInfo.NO_DEFINITE_LANG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceManager {

	private Logger ServerLogger = LoggerFactory.getLogger(VoiceManager.class);

	private final Map<Voice,TTSEngine> mBestEngines;
	private final Map<Voice,Voice> mSecondaryVoices;
	private final Map<VoiceKey,Voice> mVoiceIndex;

	public VoiceManager(Collection<TTSEngine> engines, Collection<VoiceInfo> voiceInfoFromConfig) {

		// create a map of the best services for each available voice, given that two different
		// services can serve the same voice
		mBestEngines = new HashMap<Voice,TTSEngine>(); {
			TTSTimeout timeout = new TTSTimeout();
			int timeoutSecs = 5;
			for (TTSEngine tts : engines) {
				try {
					timeout.enableForCurrentThread(timeoutSecs);
					Collection<Voice> voices = tts.getAvailableVoices();
					if (voices != null)
						for (Voice v : voices) {
							TTSEngine competitor = mBestEngines.get(v);
							if (competitor == null
							    || competitor.getOverallPriority() < tts.getOverallPriority()) {
								mBestEngines.put(v, tts);
							}
						}
				} catch (SynthesisException e) {
					ServerLogger.error("error while retrieving the voices of "
					                   + TTSServiceUtil.displayName(tts.getProvider()));
					ServerLogger.debug(TTSServiceUtil.displayName(tts.getProvider())
					                   + " getAvailableVoices error: " + getStack(e));
				} catch (InterruptedException e) {
					ServerLogger.error("timeout while retrieving the voices of "
					                   + TTSServiceUtil.displayName(tts.getProvider())
					                   + " (exceeded " + timeoutSecs + " seconds)");
				} finally {
					timeout.disable();
				}
			}
			timeout.close();
		}

		// get mappings from voice properties (language/gender) to voice (name/engine)
		List<VoiceInfo> voiceInfo = new ArrayList<>();

		// get info from configuration
		voiceInfo.addAll(voiceInfoFromConfig);

		// get info from engines (lowest priority)
		for (Voice v : mBestEngines.keySet())
			if (v.getLocale().isPresent() && v.getGender().isPresent())
				voiceInfo.add(new VoiceInfo(v,
				                            v.getLocale().get(),
				                            v.getGender().get(),
				                            0));

		// voices can also be applied to less specific locales (without region tag)
		final float priorityVariantPenalty = 0.1f;
		List<VoiceInfo> derivedVoiceInfo = new ArrayList<>(); {
			for (VoiceInfo vi : voiceInfo)
				if (!vi.isMultiLang()) {
					Locale shortLang = new Locale(vi.language.getLanguage());
					if (!vi.language.equals(shortLang))
						derivedVoiceInfo.add(new VoiceInfo(vi.voice, shortLang, vi.gender,
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
		List<VoiceInfo> availableVoiceInfo = voiceInfo.stream()
		                                              .filter(v -> mBestEngines.containsKey(v.voice))
		                                              .collect(Collectors.toList());

		// create map of the best fallback for each voice
		// engine is more important than gender and priority, gender is more important than priority
		mSecondaryVoices = new HashMap<Voice,Voice>(); {
			for (boolean sameEngine : new boolean[]{true, false})
				for (boolean sameGender : new boolean[]{true, false})
					for (VoiceInfo best : voiceInfo)
						if (!mSecondaryVoices.containsKey(best.voice))
							for (VoiceInfo fallback : availableVoiceInfo)
								if (!fallback.equals(best))
									if (fallback.isMultiLang()) {
										if (!sameGender && !sameEngine) {
											// multilang fallback voices are only considered when gender
											// and engine are not criteria so as to prevent the algo
											// from choosing a multilang voice with the same engine over
											// a regular voice with a different engine.
											mSecondaryVoices.put(best.voice, fallback.voice);
											break;
										}
									} else if (fallback.language.equals(best.language)
									           && (!sameGender
									               || fallback.gender.equals(best.gender))
									           && (!sameEngine
									               || fallback.voice.engine.equals(best.voice.engine))) {
										mSecondaryVoices.put(best.voice, fallback.voice);
										break;
									}
		}

		voiceInfo = availableVoiceInfo;

		// create index of voices with language, engine and gender as keys
		Set<Locale> allLangs = new HashSet<Locale>(); {
			for (VoiceInfo vi : voiceInfo) allLangs.add(vi.language);
			allLangs.remove(VoiceInfo.NO_DEFINITE_LANG); }
		mVoiceIndex = new HashMap<VoiceKey,Voice>(); {
			for (VoiceInfo vi : voiceInfo) {
				if (vi.isMultiLang())
					// this is to make sure that multi-lang voice wins from regular voice if it has
					// a higher priority
					for (Locale l : allLangs)
						for (boolean sameEngine : new boolean[]{true, false})
							for (boolean sameGender : new boolean[]{true, false}) {
								VoiceKey k = new VoiceKey(l,
								                          sameGender ? vi.gender : null,
								                          sameEngine ? vi.voice.engine : null);
								if (!mVoiceIndex.containsKey(k))
									mVoiceIndex.put(k, vi.voice);
							}
				for (boolean sameEngine : new boolean[]{true, false})
					for (boolean sameGender : new boolean[]{true, false}) {
						VoiceKey k = new VoiceKey(vi.language,
						                          sameGender ? vi.gender : null,
						                          sameEngine ? vi.voice.engine : null);
						if (!mVoiceIndex.containsKey(k))
							mVoiceIndex.put(k, vi.voice);
					}
			}
		}

		// log
		StringBuilder sb = new StringBuilder("Available voices:");
		for (Entry<Voice,TTSEngine> e : mBestEngines.entrySet())
			sb.append("\n* ")
			  .append(e.getKey())
			  .append(" by ")
			  .append(TTSServiceUtil.displayName(e.getValue().getProvider()));
		ServerLogger.info(sb.toString());
		sb = new StringBuilder("Voice selection data:");
		for (VoiceInfo vi : voiceInfo)
			sb.append("\n* {")
			  .append("locale:").append(vi.language == NO_DEFINITE_LANG ? "*" : vi.language)
			  .append(", gender:").append(vi.gender)
			  .append("}")
			  .append(" -> ")
			  .append(vi.voice);
		ServerLogger.info(sb.toString());
	}

	/**
	 * @return null if no voice is available for the given parameters.
	 * @param voiceEngine is null if unknown
	 * @param voiceName is null if unknown
	 * @param lang is null if unknown
	 * @param gender is null if unknown
	 * @param exactMatch[0] will be set to True if an exact match is found. The
	 *            argument can be null if the returned information is not
	 *            required.
	 */
	public Voice findAvailableVoice(String voiceEngine, String voiceName, String lang,
	                                String gender, boolean[] exactMatch) {
		return findAvailableVoice(voiceEngine, voiceName, lang, Gender.of(gender),
		                          exactMatch == null ? new boolean[1] : exactMatch);
	}

	public Voice findSecondaryVoice(Voice v) {
		return mSecondaryVoices.get(v);
	}

	/**
	 * @param voice is an available voice.
	 * @return the best TTS Engine for @param voice. It can return an engine
	 *         whose OSGi service is no longer enable.
	 */
	public TTSEngine getTTS(Voice voice) {
		return mBestEngines.get(voice);
	}

	private Voice findAvailableVoice(String voiceEngine, String voiceName, String lang,
	                                 Gender gender, boolean[] exactMatch) {

		if (voiceEngine != null && !voiceEngine.isEmpty() && voiceName != null && !voiceName.isEmpty()) {
			Voice preferredVoice = new Voice(voiceEngine, voiceName);
			if (mBestEngines.containsKey(preferredVoice)) {
				exactMatch[0] = true;
				// don't return preferredVoice because it might have the wrong case
				for (Voice v : mBestEngines.keySet())
					if (preferredVoice.equals(v))
						return v;
				// never reached
			} else {
				Voice fallback = mSecondaryVoices.get(preferredVoice);
				if (fallback != null) {
					exactMatch[0] = false;
					return fallback;
				}
			}
		}

		exactMatch[0] = false;

		Locale loc;
		try {
			loc = VoiceInfo.tagToLocale(lang);
		} catch (UnknownLanguage e) {
			return null;
		}

		Locale shortLoc = new Locale(loc.getLanguage());
		Voice result;

		// engine is more important than gender and priority, gender is more important than priority
		exactMatch[0] = (voiceName == null);
		if ((result = mVoiceIndex.get(new VoiceKey(loc, gender, voiceEngine))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(shortLoc, gender, voiceEngine))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, gender, voiceEngine))) != null)
			return result;
		exactMatch[0] = (voiceName == null && gender == null);
		if ((result = mVoiceIndex.get(new VoiceKey(loc, voiceEngine))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(shortLoc, voiceEngine))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, voiceEngine))) != null)
			return result;
		exactMatch[0] = (voiceName == null && voiceEngine == null);
		if ((result = mVoiceIndex.get(new VoiceKey(loc, gender))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(shortLoc, gender))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(NO_DEFINITE_LANG, gender))) != null)
			return result;
		exactMatch[0] = (voiceName == null && voiceEngine == null && gender == null);
		if ((result = mVoiceIndex.get(new VoiceKey(loc))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(shortLoc))) != null)
			return result;
		if ((result = mVoiceIndex.get(new VoiceKey(NO_DEFINITE_LANG))) != null)
			return result;

		return null;
	}

	private static String getStack(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}

	static private class VoiceKey {
		public String engine;
		public Locale lang;
		public Gender gender;

		public VoiceKey(Locale lang) {
			this.lang = lang;
		}

		public VoiceKey(Locale lang, Gender gender) {
			this.lang = lang;
			this.gender = gender;
		}

		public VoiceKey(Locale lang, String engine) {
			this.lang = lang;
			this.engine = engine;
		}

		public VoiceKey(Locale lang, Gender gender, String engine) {
			this.lang = lang;
			this.gender = gender;
			this.engine = engine;
		}

		public int hashCode() {
			int res = this.lang.hashCode();
			if (this.gender != null)
				res ^= this.gender.hashCode();
			if (this.engine != null)
				res ^= this.engine.hashCode();
			return res;
		}

		public boolean equals(Object other) {
			VoiceKey o = (VoiceKey) other;
			return lang.equals(o.lang)
			        && (gender == o.gender || (gender != null && gender.equals(o.gender)))
			        && (engine == o.engine || (engine != null && engine.equals(o.engine)));
		}
	}
}
