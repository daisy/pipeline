package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.daisy.pipeline.braille.common.util.Strings;

/**
 * Identification of a braille system, including details on contraction levels, specialized braille
 * systems, and additional elements relevant to braille production.
 *
 * For more info see the <a
 * href="https://daisy.github.io/ebraille/published/registries/codes/">eBraille Braille Codes
 * Registry</a>.
 */
public class BrailleCode {

	/**
	 * The level of contraction or complexity in the braille code
	 */
	public enum Grade {
		GRADE0,
		GRADE1,
		GRADE1_2, // not in spec!
		GRADE1_3, // not in spec!
		GRADE1_4, // not in spec!
		GRADE1_5, // not in spec!
		GRADE2,
		GRADE3,   // not in spec!
		GRADE4,   // not in spec!
		/** code does not use contractions or a grading system */
		NO_GRADE;

		@Override
		public String toString() {
			return name().toLowerCase().replaceAll("(?<=[0-9])_(?=[0-9])", ".")
			                           .replace("_", "-");
		}
	}

	/**
	 * Specialized systems
	 */
	public enum Specialization {
		/** 8-dot computer braille */
		COMP8,
		/** 6-dot computer braille */
		COMP6,
		/** code is only concerned with mathematics */
		MATH,
		/** code is only concerned with music */
		MUSIC,
		/** code is only concerned with phonetics */
		PHONETIC;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public String getSystem() {
		return system;
	}

	public Grade getGrade() {
		return grade;
	}

	public Optional<Specialization> getSpecialization() {
		return specialization;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BrailleCode))
			return false;
		BrailleCode that = (BrailleCode)other;
		if (!that.id.equals(this.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Return a IETF BCP 47 language tag with the braille code identifier integrated as a "t"
	 * (transformed content) extension (RFC 6497) with a "x0" (private use transform) part.
	 *
	 * @param sourceLanguage The language tag of the source (untransformed) content.
	 * @return the language tag of the braille content, which contains the "Brai" script subtag and
	 *         the braille code as a "t" extention.
	 */
	public Locale toLanguageTag(Locale sourceLanguage) {
		Locale.Builder b = new Locale.Builder();
		b.setLocale(sourceLanguage);
		b.setScript("Brai");
		b.setExtension('t', sourceLanguage.toLanguageTag().toLowerCase() + "-x0-" + idForLanguageTag);
		BrailleCode prev = idForLanguageTagCache.put(idForLanguageTag, this);
		if (prev != null && !prev.equals(this))
			throw new IllegalStateException(); // collision
		return b.build();
	}

	/**
	 * Get braille code from a IETF BCP 47 language tag. The language tag is expected to contain a
	 * "t" extension (RFC 6497) with a "x0" (private use transform) part, and the subtags after the
	 * "x0" subtag are expected to form the identifier of the braille code.
	 */
	public static BrailleCode fromLanguageTag(Locale language) throws IllegalArgumentException {
		String t = language.getExtension('t');
		if (t == null)
			throw new IllegalArgumentException("Language tag does not contain 't' extension: " + language);
		int i = t.indexOf("-x0-");
		if (i < 0)
			throw new IllegalArgumentException("Language tag does not contain 't' extension with 'x0' part: " + language);
		t = t.substring(i + 4);
		// HACK!
		// For now, because the BrailleCode feature is only used in the context of
		// dtbook-to-ebraille to pass braille code information from LiblouisTranslatorImpl through
		// language tags, and these language tags do not end up in the final eBraille publication,
		// we can put whatever we want in them. To simplify things, we generate the language tag
		// using a simple but lossy algorithm. For this reason, we don't try to parse it again, but
		// rather cache it when it is returned from toLanguageTag() (and hope there are no
		// collisions).
		BrailleCode cached = idForLanguageTagCache.get(t);
		if (cached == null)
			throw new IllegalArgumentException(); // should not happen if tag came from toLanguageTag()
		return cached;
	}

	public static BrailleCode parse(String identifier) throws IllegalArgumentException {
		List<String> parts = new ArrayList<>();
		for (String part : identifier.split(" "))
			if (part.length() == 0)
				throw new IllegalArgumentException();
			else
				parts.add(part);
		if (parts.size() < 2)
			throw new IllegalArgumentException("Missing grade");
		String lastPart = parts.get(parts.size() - 1);
		parts.remove(parts.size() - 1);
		Grade grade;
		Specialization specialization = null; {
			try {
				grade = Grade.valueOf(lastPart.replaceAll("[-.]", "_").toUpperCase());
			} catch (IllegalArgumentException e) {
				if (parts.size() < 2)
					throw new IllegalArgumentException("Could not parse grade: " + lastPart);
				String secondToLastPart = parts.get(parts.size() - 1);
				parts.remove(parts.size() - 1);
				try {
					grade = Grade.valueOf(secondToLastPart.replaceAll("[-.]", "_").toUpperCase());
				} catch (IllegalArgumentException ee) {
					throw new IllegalArgumentException("Could not parse grade: " + secondToLastPart + " or " + lastPart);
				}
				try {
					specialization = Specialization.valueOf(lastPart.toUpperCase());
				} catch (IllegalArgumentException ee) {
					throw new IllegalArgumentException("Could not parse specialization: " + lastPart);
				}
			}
		}
		return new BrailleCode(Strings.join(parts, " "), grade, specialization);
	}

	private final String id;
	private final String system;
	private final Grade grade;
	private final Optional<Specialization> specialization;
	private final String idForLanguageTag; // for inclusion in language tag, see https://github.com/daisy/ebraille/issues/291
	private static final Map<String,BrailleCode> idForLanguageTagCache = new HashMap<>();

	/**
	 * Note that the {@code system} parameter, as well as the combination with {@code grade} and
	 * {@code specialization}, is currently not validated, as there are no registered braille codes
	 * at this point.
	 */
	public BrailleCode(String system, Grade grade, Specialization specialization) {
		if (system == null || grade == null)
			throw new IllegalArgumentException();
		this.system = system;
		this.grade = grade;
		this.specialization  = Optional.ofNullable(specialization);
		this.id = system + " "
			+ grade
			+ (specialization == null ? "" : (" " + specialization));
		List<String> languageSubtags = new ArrayList<>(); {
			for (String part : system.split(" ")) {
				part = part.toLowerCase();
				part = part.replaceAll("[^a-z0-9]", "");
				if (part.length() < 3) // too short to make into a language subtag
					throw new IllegalArgumentException();
				if (part.length() > 8) // too long to make into a language subtag
					part = part.substring(0, 8);
				languageSubtags.add(part);
			}
			languageSubtags.add(grade.toString().replace(".", "")   // grade1.5 -->  grade15
			                                    .replace("-", "")); // no-grade --> nograde
			if (specialization != null)
				languageSubtags.add(specialization.toString());
		}
		this.idForLanguageTag = Strings.join(languageSubtags, "-");
	}
}

