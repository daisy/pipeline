package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

class LatexHyphenator extends AbstractHyphenator {
	private final HyphenationConfig hyphenator;
	
	LatexHyphenator(HyphenationConfig hyphenator) throws HyphenatorConfigurationException {
		this.hyphenator = hyphenator;
		this.beginLimit = this.hyphenator.getDefaultBeginLimit();
		this.endLimit = this.hyphenator.getDefaultEndLimit();
	}

	@Override
	public String hyphenate(String phrase) {
		return hyphenator.getHyphenator().hyphenate(phrase, getBeginLimit(), getEndLimit());
	}

}
