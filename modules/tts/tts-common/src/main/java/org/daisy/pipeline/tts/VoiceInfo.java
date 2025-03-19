package org.daisy.pipeline.tts;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class VoiceInfo {
	private static Pattern localePattern = Pattern
	        .compile("(\\p{Alpha}{2})(?:[-_](\\p{Alpha}{2}))?(?:[-_](\\p{Alnum}{1,8}))*");
	
	private final static Logger logger = LoggerFactory.getLogger(VoiceInfo.class);

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
            /*
             *  Se han detectado dos casos que idiomas devuelto por el el ws de amazon (polly) que no se corresponden con locales,
             *  por lo que se decide realizar una transformación a un locale que no de error ya que unicamente se utilizan para formar parte de la clave de un mapa
             *  donde se almacenan las voces disponibles:
             *      - arb: se corresponde con idioma árabe y se sustituye por el local ar con mismo significado.
             *      - cmn-CN: se corresponde con idiona chino mandarín, se sustituye por el locale zh_CN que se corresponde con el chino hablado en china.
             */
            if(langtag.equals("arb")) {
                langtag = "ar";
            } else if(langtag.equals("cmn-CN")) {
                langtag = "zh_CN";
            }
			Matcher m = localePattern.matcher(langtag.toLowerCase());
			if (m.matches()) {
				locale = new Locale(m.group(1), m.group(2) != null ? m.group(2) : "");
			}
		}
		
		if (locale == null)
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
		
//		logger.info("VoiceInfo: {} {} {} {} {}",this.voiceEngine, this.voiceName, this.language, this.priority, this.gender);
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
	
	public boolean isMultiLang(){
		return this.language == NO_DEFINITE_LANG;
	}
	
	@Override
	public String toString() {
		return "VoiceInfo: " + this.voiceEngine + " - " + this.voiceName + " - " + this.language + " - " + this.priority + " - " + this.gender;
	}

	public Gender gender;
	public String voiceName;
	public String voiceEngine;
	public Locale language;
	public float priority;
	
	public static Locale NO_DEFINITE_LANG = new Locale("mul");
}
