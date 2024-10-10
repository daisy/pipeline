package org.daisy.pipeline.tts;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Collections2;

import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.LanguageRange;

/**
 * Voice instances are expected to be created only by TTSEngine/TTSService implementations
 */
public class Voice {

	// the upper-case versions need to be kept because some TTS Processors like SAPI
	// are case-sensitive. Lower-case versions are only used for comparison.
	private final String engine;
	private final String name;
	private final MarkSupport markSupport;
	private final Collection<LanguageRange> locale;
	private final Optional<Gender> gender;
	private final String id;

	public enum MarkSupport {
		DEFAULT,
		MARK_SUPPORTED,
		MARK_NOT_SUPPORTED
	}

	public Voice(String engine, String name) {
		this(engine, name, Collections.emptyList(), null, MarkSupport.DEFAULT);
	}

	/**
	 * @throws IllegalArgumentException if language or gender can not be parsed.
	 */
	public Voice(String engine, String name, Locale locale, Gender gender) throws IllegalArgumentException {
		this(engine,
		     name,
		     locale != null ? new LanguageRange(locale) : null,
		     gender);
	}

	public Voice(String engine, String name, LanguageRange locale, Gender gender) throws IllegalArgumentException {
		this(engine,
		     name,
		     Optional.ofNullable(locale).map(x -> listOf(x)).orElseGet(Collections::emptyList),
		     gender,
		     MarkSupport.DEFAULT);
	}

	public Voice(String engine, String name, Collection<Locale> locale, Gender gender) throws IllegalArgumentException {
		this(engine, name, Collections2.transform(locale, LanguageRange::new), gender, MarkSupport.DEFAULT);
	}

	public Voice(String engine, String name, Collection<LanguageRange> locale, Gender gender, MarkSupport markSupport)
			throws IllegalArgumentException {
		if (engine == null || engine.isEmpty() || name == null || name.isEmpty())
			throw new IllegalArgumentException();
		// we keep the strings in their full case form because some engines might be case sensitive
		this.engine = engine;
		this.name = name;
		this.locale = Collections.unmodifiableCollection(locale);
		this.gender = Optional.ofNullable(gender);
		this.markSupport = markSupport;
		synchronized (ids) {
			if (ids.containsKey(this))
				id = ids.get(this);
			else {
				Integer i = voiceCounter.get(engine.toLowerCase());
				if (i == null)
					i = 1;
				else
					i++;
				id = engine.toLowerCase() + "-" + i;
				ids.put(this, id);
				voiceCounter.put(engine.toLowerCase(), i);
			}
		}
	}

	private final static Map<Voice,String> ids = new HashMap<>();
	private final static Map<String,Integer> voiceCounter = new HashMap<>();

	// see List.of() method in Java 9
	private static <E> List<E> listOf(E element) {
		if (element == null)
			throw new NullPointerException();
		return Collections.unmodifiableList(Arrays.asList(element));
	}

	public String getName() {
		return name;
	}

	public String getEngine() {
		return engine;
	}

	public MarkSupport getMarkSupport() {
		return markSupport;
	}

	/**
	 * The language range(s) of this voice, or empty if it is not known.
	 */
	public Collection<LanguageRange> getLocale() {
		return locale;
	}

	/**
	 * The gender of this voice, or absent if it is not known.
	 */
	public Optional<Gender> getGender() {
		return gender;
	}

	/**
	 * A unique ID.
	 *
	 * This method is consistent with {@link #equals}: it returns the same ID for equal voices,
	 * and returns distinct IDs for distinct voices.
	 */
	public String getID() {
		return id;
	}

	public int hashCode() {
		return engine.toLowerCase().hashCode() ^ name.toLowerCase().hashCode();
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		Voice v2 = (Voice)other;
		return engine.equalsIgnoreCase(v2.engine) && name.equalsIgnoreCase(v2.name);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("{");
		s.append("engine: ").append(engine);
		s.append(", ").append("name: ").append(name);
		if (!locale.isEmpty()) {
			s.append(", ").append("locale: ");
			if (locale.size() > 1) s.append("\"");
			s.append(LanguageRange.toString(locale));
			if (locale.size() > 1) s.append("\"");
		}
		if (gender.isPresent()) {
			s.append(", ").append("gender:").append(gender.get());
		}
		s.append("}");
		return s.toString();
	}
}
