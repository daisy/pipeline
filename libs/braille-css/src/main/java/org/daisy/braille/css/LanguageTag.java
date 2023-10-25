package org.daisy.braille.css;

import java.util.IllformedLocaleException;
import java.util.Locale;

public class LanguageTag implements LanguageRange {

	private final Locale language;

	public LanguageTag(String identifier) throws IllegalArgumentException {
		try {
			language = (new Locale.Builder()).setLanguageTag(identifier.replace('_','-')).build();
		} catch (IllformedLocaleException e) {
			throw new IllegalArgumentException("Language tag '" + identifier + "' could not be parsed", e);
		}
	}

	@Override
	public String toString() {
		return language.toLanguageTag();
	}

	@Override
	public String toString(int depth) {
		return toString();
	}

	@Override
	public boolean matches(Locale language) {
		String thisTag = this.language.toLanguageTag().toLowerCase();
		String otherTag = language.toLanguageTag().toLowerCase();
		return otherTag.equals(thisTag) || otherTag.startsWith(thisTag + "-");
	}
}
