package org.daisy.dotify.impl.hyphenator.latex;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

class LatexHyphenator extends AbstractHyphenator {
	private final HyphenationConfig hyphenator;
	
	LatexHyphenator(String locale) throws HyphenatorConfigurationException {
		this.hyphenator = LatexHyphenatorCore.getInstance().getHyphenator(locale);
		this.beginLimit = hyphenator.getDefaultBeginLimit();
		this.endLimit = hyphenator.getDefaultEndLimit();
	}
	
	static boolean supportsLocale(String locale) {
		return LatexHyphenatorCore.getInstance().supportsLocale(locale);
	}

	public String hyphenate(String phrase) {
		return hyphenator.getHyphenator().hyphenate(phrase, getBeginLimit(), getEndLimit());
	}

}
