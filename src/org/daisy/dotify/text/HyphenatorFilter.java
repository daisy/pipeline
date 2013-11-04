package org.daisy.dotify.text;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;

/**
 * Provides a hyphenating string filter. This filter will hyphenate the
 * filter input using the supplied hyphenator.
 * 
 * @author Joel Håkansson
 *
 */
public class HyphenatorFilter implements StringFilter {
	private final HyphenatorInterface hyphenator;

	public HyphenatorFilter(HyphenatorFactoryMakerService factory, String locale) throws HyphenatorConfigurationException {
		this(factory.newHyphenator(locale));
	}
	
	public HyphenatorFilter(HyphenatorInterface hyphenator) {
		this.hyphenator = hyphenator;
	}

	public int getBeginLimit() {
		return hyphenator.getBeginLimit();
	}

	public void setBeginLimit(int beginLimit) {
		hyphenator.setBeginLimit(beginLimit);
	}

	public int getEndLimit() {
		return hyphenator.getEndLimit();
	}

	public void setEndLimit(int endLimit) {
		hyphenator.setEndLimit(endLimit);
	}
	
	public String filter(String str) {
		return hyphenator.hyphenate(str);
	}
	
}