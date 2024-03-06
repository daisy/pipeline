package org.daisy.pipeline.common;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NormalizeLang {

	/**
	 * Mapping from three- to two-letter language tags
	 */
	private static Map<String,String> langMap = null;

	/**
	 * Convert three-letter language subtag to two-letter tag if possible.
	 */
	public static Locale normalize(Locale locale) {
		if (langMap == null) {
			String[] allLanguages = Locale.getISOLanguages(); // all two-letter language tags
			langMap = new HashMap<String,String>(allLanguages.length);
			for (String l : allLanguages)
				langMap.put(new Locale(l).getISO3Language(), l);
		}
		String twoLetterLang = langMap.get(locale.getLanguage()); // convert to two-letter
		if (twoLetterLang != null)
			return (new Locale.Builder()).setLocale(locale).setLanguage(twoLetterLang).build();
		return locale;
	}
}
