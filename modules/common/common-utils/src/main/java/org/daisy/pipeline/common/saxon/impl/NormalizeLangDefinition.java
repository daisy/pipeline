package org.daisy.pipeline.common.saxon.impl;

import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "NormalizeLang",
	service = { ExtensionFunctionProvider.class }
)
public class NormalizeLangDefinition extends ReflexiveExtensionFunctionProvider {

	public NormalizeLangDefinition() {
		super(NormalizeLang.class);
	}

	public static class NormalizeLang {

		/**
		 * Mapping from three- to two-letter language tags
		 */
		private static Map<String,String> langMap = null;

		/**
		 * Convert three-letter language subtag to two-letter tag if possible.
		 */
		public static String normalize(String locale) {
			try {
				return normalize((new Locale.Builder()).setLanguageTag(locale.replace('_','-')).build()).toLanguageTag();
			} catch (IllformedLocaleException e) {
				// ignore
			}
			return locale;
		}

		private static Locale normalize(Locale locale) {
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
}
