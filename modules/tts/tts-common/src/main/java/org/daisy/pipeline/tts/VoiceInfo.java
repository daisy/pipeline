package org.daisy.pipeline.tts;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class VoiceInfo {

	public Gender gender;
	public String voiceName;
	public String voiceEngine;
	public Locale language;
	public float priority;

	public static Locale NO_DEFINITE_LANG = new Locale("mul");
	public static Gender NO_DEFINITE_GENDER = Gender.ANY;

	private static Pattern localePattern = Pattern
	        .compile("(\\p{Alpha}{2})(?:[-_](\\p{Alpha}{2}))?(?:[-_](\\p{Alnum}{1,8}))*");

	public enum Gender {
		ANY("*", "neutral"),
		MALE_ADULT("male", "man", "male-adult"),
		MALE_CHILD("boy", "male-young", "male-child"),
		MALE_ELDERLY("man-old", "male-old", "male-elder", "man-elder", "male-elderly", "man-elderly"),
		FEMALE_CHILD("girl", "female-young", "female-child"),
		FEMALE_ADULT("woman", "female", "female-adult"),
		FEMALE_ELDERLY("woman-old", "female-old", "woman-elder", "female-elder", "woman-elderly", "female-elderly");

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
					}
				}
			}
		}

		public static Gender of(String gender) {
			return gender == null ? null : lookup.get(gender.toLowerCase().replace("_", "-"));
		}

		@Override
		public String toString() {
			return this == ANY ? "*" : super.toString().toLowerCase().replace("_", "-");
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
		Locale locale = Locale.forLanguageTag(langtag.replace("_", "-"));
		if (locale == null || "und".equals(locale.toLanguageTag()))
			throw new UnknownLanguage(langtag);
		return locale;
	}

	public VoiceInfo(String voiceEngine, String voiceName, String language, Gender gender, float priority)
			throws UnknownLanguage {
		this(voiceEngine, voiceName, tagToLocale(language), gender, priority);
	}

	VoiceInfo(String voiceEngine, String voiceName, Locale locale, Gender gender, float priority) {
		Preconditions.checkNotNull(gender);
		Preconditions.checkNotNull(voiceName);
		Preconditions.checkNotNull(voiceEngine);
		this.voiceEngine = voiceEngine;
		this.voiceName = voiceName;
		this.language = locale;
		this.priority = priority;
		this.gender = gender;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + Float.floatToIntBits(priority);
		result = prime * result + ((voiceEngine == null) ? 0 : voiceEngine.hashCode());
		result = prime * result + ((voiceName == null) ? 0 : voiceName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VoiceInfo other = (VoiceInfo) obj;
		if (gender != other.gender)
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (Float.floatToIntBits(priority) != Float.floatToIntBits(other.priority))
			return false;
		if (voiceEngine == null) {
			if (other.voiceEngine != null)
				return false;
		} else if (!voiceEngine.equals(other.voiceEngine))
			return false;
		if (voiceName == null) {
			if (other.voiceName != null)
				return false;
		} else if (!voiceName.equals(other.voiceName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String s = "";
		if (voiceEngine != null) {
			s += ("engine=" + voiceEngine);
		}
		if (voiceName != null) {
			if (s.length() > 0) s += ", ";
			s += ("name=" + voiceName);
		}
		if (language != null) {
			if (s.length() > 0) s += ", ";
			s += ("lang=" + (language == NO_DEFINITE_LANG ? "*" : language));
		}
		if (gender != null) {
			if (s.length() > 0) s += ", ";
			s += ("gender=" + (gender == NO_DEFINITE_GENDER ? "*" : gender));
		}
		{
			if (s.length() > 0) s += ", ";
			s += ("priority=" + priority);
		}
		s = "VoiceInfo[" + s + "]";
		return s;
	}

	public boolean isMultiLang(){
		return this.language == NO_DEFINITE_LANG;
	}
}
