package org.daisy.pipeline.tts;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class VoiceInfo {

	public Gender gender;
	public String voiceName;
	public String voiceEngine;
	public LanguageRange language;
	public float priority;

	public static Gender NO_DEFINITE_GENDER = Gender.ANY;

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

	public static class LanguageRange {

		private static final Pattern LANGUAGE_RANGE = Pattern.compile("(\\p{Alpha}{1,8}|\\*)(-(\\p{Alnum}{1,8}|\\*))*");

		private final String range;
		private final Locale primaryLanguageSubTag;
		private final int specificity;

		/**
		 * @throws IllegalArgumentException if the argument does not represent a valid language range
		 */
		public LanguageRange(String range) {
			if (range == null)
				throw new IllegalArgumentException();
			if ("mul".equalsIgnoreCase(range)) {
				this.range = "*";
				primaryLanguageSubTag = null;
				specificity = 0;
			} else {
				if (!LANGUAGE_RANGE.matcher(range).matches())
					throw new IllegalArgumentException("Not a valid language range: " + range);
				if (range.equals("*")) {
					primaryLanguageSubTag = null;
					specificity = 0;
				} else if (!range.contains("*"))
					try {
						Locale locale = (new Locale.Builder()).setLanguageTag(range).build();
						primaryLanguageSubTag = new Locale(locale.getLanguage());
						range = locale.toLanguageTag();
						specificity = range.split("-").length;
					} catch (IllformedLocaleException e) {
						throw new IllegalArgumentException("Not a valid language range: '" + range, e);
					}
				else
					throw new IllegalArgumentException("FIXME: Extended language ranges not implemented yet");
				this.range = range;
			}
		}

		public LanguageRange(Locale locale) {
			this(locale == null ? null : locale.toLanguageTag());
		}

		/**
		 * Whether the language range matches the given locale.
		 */
		public boolean matches(Locale locale) {
			if (locale == null)
				throw new IllegalArgumentException();
			if (this.range.equals("*"))
				return true;
			String tag = locale.toLanguageTag().toLowerCase();
			String range = this.range.toLowerCase();
			return tag.equals(range) || tag.startsWith(range + "-");
		}

		/**
		 * Whether the language list matches the given locale.
		 */
		public static boolean matches(Collection<LanguageRange> languageList, Locale locale) {
			if (locale == null)
				throw new IllegalArgumentException();
			for (LanguageRange l : languageList)
				if (l.matches(locale))
					return true;
			return false;
		}

		/**
		 * The primary language subtag if it is not equal to "*", {@code null} otherwise.
		 */
		public Locale getPrimaryLanguageSubTag() {
			return primaryLanguageSubTag;
		}

		/**
		 * To determine whether a language range is more specific than another language range.
		 */
		public int getSpecificity() {
			return specificity;
		}

		@Override
		public String toString() {
			return range;
		}

		/**
		 * Print language list as comma-separated list.
		 */
		public static String toString(Collection<LanguageRange> languageList) {
			StringBuilder s = new StringBuilder();
			boolean first = true;
			for (LanguageRange l : languageList) {
				if (!first) s.append(", ");
				s.append(l);
				first = false;
			}
			return s.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + range.toLowerCase().hashCode();
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
			LanguageRange other = (LanguageRange)obj;
			if (!range.equalsIgnoreCase(other.range))
				return false;
			return true;
		}
	}

	/**
	 * @throws IllegalArgumentException if language or gender can not be parsed.
	 */
	public VoiceInfo(String voiceEngine, String voiceName, String language, Gender gender, float priority)
			throws IllegalArgumentException {
		this(voiceEngine, voiceName, new LanguageRange(language), gender, priority);
	}

	public VoiceInfo(String voiceEngine, String voiceName, Locale language, Gender gender, float priority)
			throws IllegalArgumentException {
		this(voiceEngine, voiceName, new LanguageRange(language), gender, priority);
	}

	public VoiceInfo(String voiceEngine, String voiceName, LanguageRange language, Gender gender, float priority)
			throws IllegalArgumentException {
		Preconditions.checkNotNull(voiceEngine);
		Preconditions.checkNotNull(voiceName);
		Preconditions.checkNotNull(language);
		Preconditions.checkNotNull(gender);
		this.voiceEngine = voiceEngine;
		this.voiceName = voiceName;
		this.language = language;
		this.gender = gender;
		this.priority = priority;
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
			s += ("lang=" + language);
		}
		if (gender != null) {
			if (s.length() > 0) s += ", ";
			s += ("gender=" + gender);
		}
		{
			if (s.length() > 0) s += ", ";
			s += ("priority=" + priority);
		}
		s = "VoiceInfo[" + s + "]";
		return s;
	}
}
