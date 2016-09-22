package org.daisy.pipeline.tts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceManager {

	private Logger ServerLogger = LoggerFactory.getLogger(VoiceManager.class);

	private List<VoiceInfo> mVoicePriorities;
	private Map<VoiceKey, Voice> mVoiceFullDescription = new HashMap<VoiceKey, Voice>();
	private Map<VoiceKey, Voice> mVoiceGenderMissing = new HashMap<VoiceKey, Voice>();
	private Map<VoiceKey, Voice> mVoiceEngineMissing = new HashMap<VoiceKey, Voice>();
	private Map<Locale, Voice> mVoiceLangOnly = new HashMap<Locale, Voice>(); //both gender and engine are missing
	private Map<Voice, TTSEngine> mBestEngines = new HashMap<Voice, TTSEngine>();
	private Map<Voice, Voice> mSecondVoices = new HashMap<Voice, Voice>();
	private static List<VoiceInfo> BuiltinVoices;
	
	//For now, we are keeping only one 'multi-lang' voice, which will be selected no matter which
	//gender or engine is requested. So we are not considering the best multilang voice given an
	//engine or a gender, though that would make sense for future improvements.
	private Voice mBestMultiLangVoice; 

	static {
		//the following engine names must be consistent with the ones provided by the TTSService
		final String eSpeak = "espeak";
		final String att = "att";
		final String microsoft = "sapi";
		final String acapela = "acapela";
		final String osx = "osx-speech";
		final int eSpeakPriority = 1;
		final int ATT16Priority = 10;
		final int ATT8Priority = 7;
		final int AcapelaPriority = 12;
		final int OSXSpeechPriority = 5;

		//TODO: move this array to an external XML file (ideally a file editable by users).

		try{
			BuiltinVoices = Arrays
		        .asList(
		        //eSpeak:
		        		new VoiceInfo(eSpeak, "french", "fr", Gender.MALE_ADULT, eSpeakPriority),
		                new VoiceInfo(eSpeak, "danish", "da", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "german", "de", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "greek", "el", Gender.MALE_ADULT, eSpeakPriority),
		                new VoiceInfo(eSpeak, "hindi", "hi", Gender.MALE_ADULT, eSpeakPriority),
		                new VoiceInfo(eSpeak, "hungarian", "hu", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "finnish", "fi", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "italian", "it", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "latin", "la", Gender.MALE_ADULT, eSpeakPriority),
		                new VoiceInfo(eSpeak, "norwegian", "no", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "polish", "pl", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "portugal", "pt", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "russian_test", "ru", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "swedish", "sv", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "mandarin", "zh", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "turkish", "tr", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "afrikaans", "af", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "spanish", "es", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "spanish-latin-american", "es-la",
		                        Gender.MALE_ADULT, eSpeakPriority),
		                new VoiceInfo(eSpeak, "english-us", "en", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "english-us", "en-us", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                new VoiceInfo(eSpeak, "english", "en-uk", Gender.MALE_ADULT,
		                        eSpeakPriority),
		                //AT&T:
		                new VoiceInfo(att, "alain16", "fr", Gender.MALE_ADULT, ATT16Priority),
		                new VoiceInfo(att, "alain8", "fr", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "juliette16", "fr", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "juliette8", "fr", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "arnaud16", "fr-ca", Gender.MALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "arnaud8", "fr-ca", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "klara16", "de", Gender.FEMALE_ADULT, ATT16Priority),
		                new VoiceInfo(att, "klara8", "de", Gender.FEMALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "reiner16", "de", Gender.MALE_ADULT, ATT16Priority),
		                new VoiceInfo(att, "reiner8", "de", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "anjali16", "en-uk", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "anjali8", "en-uk", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "audrey16", "en-uk", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "audrey8", "en-uk", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "charles16", "en-uk", Gender.MALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "charles8", "en-uk", Gender.MALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "mike16", "en-us", Gender.MALE_ADULT, ATT16Priority),
		                new VoiceInfo(att, "mike8", "en-us", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "claire16", "en-us", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "claire8", "en-us", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "crystal16", "en-us", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "crystal8", "en-us", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "julia16", "en-us", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "julia8", "en-us", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "lauren16", "en-us", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "lauren8", "en-us", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "mel16", "en-us", Gender.MALE_ADULT, ATT16Priority),
		                new VoiceInfo(att, "mel8", "en-us", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "ray16", "en-us", Gender.MALE_ADULT, ATT16Priority),
		                new VoiceInfo(att, "ray8", "en-us", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "rich16", "en-us", Gender.MALE_ADULT, ATT16Priority),
		                new VoiceInfo(att, "rich8", "en-us", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "alberto16", "es-us", Gender.MALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "alberto8", "es-us", Gender.MALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "rosa16", "es-us", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "rosa8", "es-us", Gender.FEMALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "francesca16", "it", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "francesca8", "it", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "giovanni16", "it", Gender.MALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "giovanni8", "it", Gender.MALE_ADULT, ATT8Priority),
		                new VoiceInfo(att, "marina16", "pt-br", Gender.FEMALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "marina8", "pt-br", Gender.FEMALE_ADULT,
		                        ATT8Priority),
		                new VoiceInfo(att, "tiago16", "pt-br", Gender.MALE_ADULT,
		                        ATT16Priority),
		                new VoiceInfo(att, "tiago8", "pt-br", Gender.MALE_ADULT, ATT8Priority),
		                //Microsoft:
		                new VoiceInfo(microsoft, "Microsoft Anna", "en-us",
		                        Gender.FEMALE_ADULT, 4), new VoiceInfo(microsoft,
		                        "Microsoft David", "en-us", Gender.MALE_ADULT, 6),
		                new VoiceInfo(microsoft, "Microsoft Zira", "en-us",
		                        Gender.FEMALE_ADULT, 6), new VoiceInfo(microsoft,
		                        "Microsoft Hazel", "en-uk", Gender.MALE_ADULT, 6),
		                new VoiceInfo(microsoft, "Microsoft Mike", "en-us", Gender.MALE_ADULT,
		                        2), new VoiceInfo(microsoft, "Microsoft Lili", "zh",
		                        Gender.FEMALE_ADULT, 3),
		                new VoiceInfo(microsoft, "Microsoft Simplified Chinese", "zh",
		                        Gender.MALE_ADULT, 2),
		                //Acapela (those are the speaker's names, not the voice names):
		                new VoiceInfo(acapela, "heather", "en-us", Gender.FEMALE_ADULT,
		                        AcapelaPriority), new VoiceInfo(acapela, "will", "en-us",
		                        Gender.MALE_ADULT, AcapelaPriority), new VoiceInfo(acapela,
		                        "tracy", "en-us", Gender.FEMALE_ADULT, AcapelaPriority),
		                new VoiceInfo(acapela, "antoine", "fr", Gender.MALE_ADULT,
		                        AcapelaPriority), new VoiceInfo(acapela, "claire", "fr",
		                        Gender.FEMALE_ADULT, AcapelaPriority), new VoiceInfo(acapela,
		                        "alice", "fr", Gender.FEMALE_ADULT, AcapelaPriority),
		                new VoiceInfo(acapela, "bruno", "fr", Gender.MALE_ADULT,
		                        AcapelaPriority),
		                // OS X
		                new VoiceInfo(osx, "Alex", "en-us", Gender.MALE_ADULT,
		                        OSXSpeechPriority), new VoiceInfo(osx, "Thomas", "fr-fr",
		                        Gender.MALE_ADULT, OSXSpeechPriority)

		        );
		} catch (UnknownLanguage e){
			throw new RuntimeException(e);
		}
	}

	public VoiceManager(Collection<TTSEngine> engines, Collection<VoiceInfo> extraVoices) {
		//build the list of voices ordered by priority
		final float priorityVariantPenalty = 0.1f;
		Set<VoiceInfo> priorities = new LinkedHashSet<VoiceInfo>(BuiltinVoices);
		priorities.addAll(extraVoices);
		mVoicePriorities = new ArrayList<VoiceInfo>(priorities);
		for (VoiceInfo vinfo : priorities) {
			if (vinfo.language != null){
				String shortLang = vinfo.language.getLanguage();
				if (!vinfo.language.equals(new Locale(shortLang))) {
					try {
						mVoicePriorities.add(new VoiceInfo(vinfo.voice, shortLang, vinfo.gender,
						        vinfo.priority - priorityVariantPenalty));
					} catch (UnknownLanguage e1) {
						//should not happen
					}
				}
			}
		}
		Comparator<VoiceInfo> reverseComp = new Comparator<VoiceInfo>() {
			@Override
			public int compare(VoiceInfo v1, VoiceInfo v2) {
				return Float.valueOf(v2.priority).compareTo(Float.valueOf(v1.priority));
			}
		};
		Collections.sort(mVoicePriorities, reverseComp);

		//Create a map of the best services for each available voice, given that two different
		//services can serve the same voice. 
		TTSTimeout timeout = new TTSTimeout();
		for (TTSEngine tts : engines) { //no possible concurrent write
			try {
				timeout.enableForCurrentThread(3);
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
				        + TTSServiceUtil.displayName(tts.getProvider()));
			} finally {
				timeout.disable();
			}
		}
		timeout.close();
		
		//get the best voice that can handle any language, if any
		for (VoiceInfo voiceInfo : mVoicePriorities) {
			if (voiceInfo.isMultiLang() && mBestEngines.containsKey(voiceInfo.voice)){
				mBestMultiLangVoice = voiceInfo.voice;
				break;
			}
		}
		
		//find all the languages. That will help us construct a multi-lang voice
		HashSet<Locale> allLangs = new HashSet<Locale>();
		for (VoiceInfo voiceInfo : mVoicePriorities)
			allLangs.add(voiceInfo.language);
		allLangs.remove(VoiceInfo.NO_DEFINITE_LANG);
		

		//Create maps for the best voices depending on the information available among
		//the language, the engine and the gender
		for (VoiceInfo voiceInfo : mVoicePriorities) {
			if (voiceInfo.isMultiLang()){
				for (Locale lang : allLangs)
					registerVoice(voiceInfo, lang);
			}
			else
				registerVoice(voiceInfo, voiceInfo.language);
		}

		/*
		 * Create a map of the best fallback voices for each language. engine is
		 * more important than priority and gender. Gender is more important
		 * than priority.
		 */
		List<VoiceInfo> sortedVoices = new ArrayList<VoiceInfo>();
		for (int i = 0; i < mVoicePriorities.size(); ++i) {
			VoiceInfo vi = mVoicePriorities.get(i);
			if (mBestEngines.containsKey(vi.voice))
				sortedVoices.add(vi);
		}

		setSecondVoices(sortedVoices, true, true);
		setSecondVoices(sortedVoices, true, false);
		setSecondVoices(sortedVoices, false, true);
		setSecondVoices(sortedVoices, false, false);

		//log the available voices
		StringBuilder sb = new StringBuilder("Available voices:");
		for (Entry<Voice, TTSEngine> e : mBestEngines.entrySet()) {
			sb.append("\n* " + e.getKey() + " by "
			        + TTSServiceUtil.displayName(e.getValue().getProvider()));
		}
		ServerLogger.info(sb.toString());
		sb = new StringBuilder("Fallback voices:");
		for (Entry<Locale, Voice> e : mVoiceLangOnly.entrySet()) {
			sb.append("\n* " + e.getKey() + ": " + e.getValue());
		}
		ServerLogger.info(sb.toString());
	}

	/**
	 * @return the list of all the voices that can be automatically chosen by
	 *         the pipeline. It can help TTS adapters to build their list of
	 *         available voices.
	 */
	public Collection<VoiceInfo> getAllPossibleVoices() {
		return mVoicePriorities;
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
		return mSecondVoices.get(v);
	}

	/**
	 * @param voice is an available voice.
	 * @return the best TTS Engine for @param voice. It can return an engine
	 *         whose OSGi service is no longer enable.
	 */
	public TTSEngine getTTS(Voice voice) {
		return mBestEngines.get(voice);
	}

	// **** private implementation *****

	private void registerVoice(VoiceInfo voiceInfo, Locale lang){
		registerVoice(mVoiceFullDescription, new VoiceKey(lang,
		        voiceInfo.gender, voiceInfo.voice.engine), voiceInfo.voice);

		registerVoice(mVoiceGenderMissing, new VoiceKey(lang,
		        voiceInfo.voice.engine), voiceInfo.voice);

		registerVoice(mVoiceEngineMissing, new VoiceKey(lang,
		        voiceInfo.gender), voiceInfo.voice);

		registerVoice(mVoiceLangOnly, lang, voiceInfo.voice);
	}
	
	private <K> void registerVoice(Map<K, Voice> voiceMap, K key, Voice v) {
		if (!voiceMap.containsKey(key) && mBestEngines.containsKey(v))
			voiceMap.put(key, v);
	}

	private <K> Voice searchVoice(Map<K, Voice> voiceMap, K key1, K key2) {
		if (voiceMap.containsKey(key1))
			return voiceMap.get(key1);

		return voiceMap.get(key2);
	}

	private Voice findAvailableVoice(String voiceEngine, String voiceName, String lang,
	        Gender gender, boolean[] exactMatch) {

		if (voiceEngine != null && !voiceEngine.isEmpty() && voiceName != null
		        && !voiceName.isEmpty()) {
			Voice preferredVoice = new Voice(voiceEngine, voiceName);
			if (mBestEngines.containsKey(preferredVoice)) {
				exactMatch[0] = true;
				return preferredVoice;
			} else {
				Voice fallback = findSecondaryVoice(preferredVoice);
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
		if (loc == null)
			return mBestMultiLangVoice;

		Locale shortLoc = new Locale(loc.getLanguage());
		Voice result;

		result = searchVoice(mVoiceFullDescription, new VoiceKey(loc, gender, voiceEngine),
		        new VoiceKey(shortLoc, gender, voiceEngine));
		if (result != null) {
			exactMatch[0] = (voiceName == null);
			return result;
		}

		result = searchVoice(mVoiceGenderMissing, new VoiceKey(loc, voiceEngine),
		        new VoiceKey(shortLoc, voiceEngine));
		if (result != null) {
			exactMatch[0] = (voiceName == null && gender == null);
			return result;
		}
		result = searchVoice(mVoiceEngineMissing, new VoiceKey(loc, gender), new VoiceKey(
		        shortLoc, gender));
		if (result != null) {
			exactMatch[0] = (voiceName == null && voiceEngine == null);
			return result;
		}

		exactMatch[0] = (voiceName == null && voiceEngine == null && gender == null);
		result = searchVoice(mVoiceLangOnly, loc, shortLoc);
		if (result != null){
			return result;
		}
		
		return mBestMultiLangVoice;
	}

	private void setSecondVoices(List<VoiceInfo> sortedAvailableVoices,
	        boolean sameEngine, boolean sameGender) {
		for (int i = 0; i < mVoicePriorities.size(); ++i) {
			VoiceInfo bestVoice = mVoicePriorities.get(i);
			if (!mSecondVoices.containsKey(bestVoice.voice)) {
				for (VoiceInfo fallback : sortedAvailableVoices) {
					if (!fallback.equals(bestVoice)){
						if (fallback.isMultiLang()){
							if (!sameGender && !sameEngine){
								//multilang fallback voices are only considered when gender and engine are not
								//criteria so as to prevent the algo from choosing a multilang voice with the
								//same engine over a regular voice with a different engine.
								mSecondVoices.put(bestVoice.voice, fallback.voice);
								break;
							}
						}
						else if (fallback.language.equals(bestVoice.language)
						        && (!sameGender || fallback.gender.equals(bestVoice.gender))
						        && (!sameEngine || fallback.voice.engine
						                .equals(bestVoice.voice.engine))) {
							mSecondVoices.put(bestVoice.voice, fallback.voice);
							break;
						}
					}
				}
			}
		}
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
