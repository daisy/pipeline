package org.daisy.pipeline.tts;

import java.util.Locale;
import java.util.Optional;

import org.daisy.pipeline.tts.VoiceInfo.Gender;

/** Voice instances are expected to be created only by TTSEngine/TTSService implementations */
public class Voice {

	public enum MarkSupport {
		DEFAULT,
		MARK_SUPPORTED,
		MARK_NOT_SUPPORTED
	}

	public Voice(String engine, String name) {
		this(engine, name, MarkSupport.DEFAULT);
	}

	public Voice(String engine, String name, MarkSupport markSupport) {
		this(engine, name, null, null, markSupport);
	}

	public Voice(String engine, String name, Locale locale, Gender gender) {
		this(engine, name, locale, gender, MarkSupport.DEFAULT);
	}

	public Voice(String engine, String name, Locale locale, Gender gender, MarkSupport markSupport) {
		//we keep the strings in their full case form because some engines might be case sensitive
		this.engine = engine == null ? "" : engine;
		this.name = name == null ? "" : name;
		this.locale = Optional.ofNullable(locale);
		this.gender = Optional.ofNullable(gender);
		this.mMarkSupport = markSupport;
		this.mEngine_lo = this.engine.toLowerCase();
		this.mName_lo = this.name.toLowerCase();
	}

	public int hashCode() {
		return mEngine_lo.hashCode() ^ mName_lo.hashCode();
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		Voice v2 = (Voice) other;
		return mEngine_lo.equals(v2.mEngine_lo) && mName_lo.equals(v2.mName_lo);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		boolean first = true;
		s.append("{");
		if (!engine.isEmpty()) {
			s.append("engine:").append(engine);
			first = false;
		}
		if (!name.isEmpty()) {
			if (!first) s.append(", ");
			s.append("name:").append(name);
			first = false;
		}
		if (locale.isPresent()) {
			if (!first) s.append(", ");
			s.append("locale:").append(locale.get());
			first = false;
		}
		if (gender.isPresent()) {
			if (!first) s.append(", ");
			s.append("gender:").append(gender.get());
		}
		s.append("}");
		return s.toString();
	}

	public MarkSupport getMarkSupport() {
		return mMarkSupport;
	}

	/**
	 * The locale of this voice, or absent if it is not known.
	 */
	public Optional<Locale> getLocale() {
		return locale;
	}

	/**
	 * The gender of this voice, or absent if it is not known.
	 */
	public Optional<Gender> getGender() {
		return gender;
	}

	//the upper-case versions need to be kept because some TTS Processors like SAPI
	//are case-sensitive. Lower-case versions are only used for comparison.
	public final String engine;
	public final String name;
	private String mEngine_lo;
	private String mName_lo;
	private final MarkSupport mMarkSupport;
	private final Optional<Locale> locale;
	private final Optional<Gender> gender;
}
