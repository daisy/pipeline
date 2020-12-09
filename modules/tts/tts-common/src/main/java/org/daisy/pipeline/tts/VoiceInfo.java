package org.daisy.pipeline.tts;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.tts.Voice.MarkSupport;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class VoiceInfo {
	private static Pattern localePattern = Pattern
	        .compile("(\\p{Alpha}{2})(?:[-_](\\p{Alpha}{2}))?(?:[-_](\\p{Alnum}{1,8}))*");

	public enum Gender {
		MALE_ADULT("male", "man", "male-adult"),
		MALE_CHILD("boy", "male-young", "male-child"),
		MALE_ELDERY("man-old", "male-old", "male-elder", "man-elder"),
		FEMALE_CHILD("girl", "female-young", "female-child"),
		FEMALE_ADULT("woman", "female", "female-adult"),
		FEMALE_ELDERY("woman-old", "female-old", "woman-elder", "female-elder");

		private final List<String> variants;

		private Gender(String... variants) {
			this.variants = Lists.newArrayList(variants);
		}

		private static final Map<String, Gender> lookup = new HashMap<String, Gender>();

		static {
			Splitter splitter = Splitter.on('-').omitEmptyStrings();
			for (Gender gender : EnumSet.allOf(Gender.class)) {
				for (String variant : gender.variants) {
					lookup.put(variant, gender);
					List<String> parts = splitter.splitToList(variant);
					if (parts.size() > 1) {
						lookup.put(parts.get(0) + parts.get(1), gender);
						lookup.put(parts.get(1) + parts.get(0), gender);
						lookup.put(parts.get(0) + '-' + parts.get(1), gender);
						lookup.put(parts.get(1) + '-' + parts.get(0), gender);
						lookup.put(parts.get(0) + '_' + parts.get(1), gender);
						lookup.put(parts.get(1) + '_' + parts.get(0), gender);
					}
				}
			}
		}

		public static Gender of(String gender) {
			return lookup.get(gender);
		}

	}
	
	public static class UnknownLanguage extends Exception{
		public UnknownLanguage(String message) {
	        super(message);
	    }
	}

	public static Locale tagToLocale(String langtag) throws UnknownLanguage {
		if (langtag == null || "*".equals(langtag) || "mul".equals(langtag))
			return NO_DEFINITE_LANG;
		
		//TODO: in Java7 we would use:
		//return Locale.forLanguageTag(lang)
		//=> this works with BCP47 tags, and should work with old tags from RFC 3066
		//TODO: use a common function for pipeline-mod-nlp and pipeline-mod-tts
		Locale locale = null;
		if (langtag != null) {
			Matcher m = localePattern.matcher(langtag.toLowerCase());
			if (m.matches()) {
				locale = new Locale(m.group(1), m.group(2) != null ? m.group(2) : "");
			}
		}
		
		if (locale == null)
			throw new UnknownLanguage(langtag);
		
		return locale;
	}

	public VoiceInfo(String voiceEngine, String voiceName, String language, Gender gender,
	        float priority) throws UnknownLanguage {
		this(voiceEngine, voiceName, MarkSupport.DEFAULT, language, gender, priority);
	}

	public VoiceInfo(String voiceEngine, String voiceName, MarkSupport markSupport,
	        String language, Gender gender, float priority) throws UnknownLanguage {
		this(new Voice(voiceEngine, voiceName, markSupport), language, gender, priority);
	}

	public VoiceInfo(Voice v, String language, Gender gender, float priority) throws UnknownLanguage {
		this(v, tagToLocale(language), gender, priority);
	}

	public VoiceInfo(Voice v, Locale language, Gender gender) {
		this(v, language, gender, -1);
	}

	VoiceInfo(Voice v, Locale locale, Gender gender, float priority) {
		Preconditions.checkNotNull(v);
		Preconditions.checkNotNull(gender);
		this.voice = v;
		this.language = locale;
		this.priority = priority;
		this.gender = gender;
	}

	@Override
	public int hashCode() {
		return this.voice.hashCode() ^ (this.language == null ? 0 : this.language.hashCode());
	}

	public boolean equals(Object other) {
		VoiceInfo o = (VoiceInfo) other;
		return voice.equals(o.voice) && ((language == null && o.language == null) ||
				(language != null && language.equals(o.language)));
	}
	
	public boolean isMultiLang(){
		return this.language == NO_DEFINITE_LANG;
	}

	public Gender gender;
	public Voice voice;
	public Locale language;
	public float priority;
	
	public static Locale NO_DEFINITE_LANG = new Locale("mul");
}
