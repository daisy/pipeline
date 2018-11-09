package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

class CWHyphenator extends AbstractHyphenator {
	private final CWHyphenatorAtom hyphenator;
	private final int accuracy;

	public CWHyphenator(String locale, int accuracy) throws HyphenatorConfigurationException {
		this.hyphenator = CWHyphenatorCore.getInstance().getHyphenator(locale);
		this.beginLimit = hyphenator.getDefaultBeginLimit();
		this.endLimit = hyphenator.getDefaultEndLimit();
		this.accuracy = accuracy;
	}

	static boolean supportsLocale(String locale) {
		return CWHyphenatorCore.getInstance().supportsLocale(locale);
	}

	@Override
	public String hyphenate(String phrase) {
		return hyphenator.hyphenate(phrase, getBeginLimit(), getEndLimit(), accuracy);
	}

}
