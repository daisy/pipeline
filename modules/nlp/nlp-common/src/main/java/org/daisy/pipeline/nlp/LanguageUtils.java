package org.daisy.pipeline.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageUtils {
	private static Pattern localePattern = Pattern
	        .compile("(\\p{Alpha}{2})(?:[-_](\\p{Alpha}{2}))?(?:[-_](\\p{Alnum}{1,8}))*");

	public static Locale stringToLanguage(String lang) {
		//TODO: in Java7 we would use:
		//return Locale.forLanguageTag(lang)
		//=> this works with BCP47 tags, and should work with old tags from RFC 3066

		Locale locale = null;
		if (lang != null) {
			Matcher m = localePattern.matcher(lang.toLowerCase());
			if (m.matches()) {
				locale = new Locale(m.group(1), m.group(2) != null ? m.group(2) : "");
			}
		}
		return locale;
	}

	//lowercase ISO 639-2/T language codes (www.loc.gov/standards/iso639-2/php/English_list.php)
	private static Set<String> RightToLeft = new HashSet<String>(Arrays.asList("yid", "ara",
	        "jpr", "per", "fas", "peo", "heb"));

	public static boolean isRightToLeft(Locale l) {
		return RightToLeft.contains(l.getISO3Language());
	}

	// one can customize the punctuation/space symbols depending on the
	// given language.

	public static String getFullStopSymbol(Locale lang) {
		return "! ";
	}

	public static String getCommaSymbol(Locale lang) {
		return ", ";
	}

	public static String getWhiteSpaceSymbol(Locale lang) {
		return " ";
	}
}
