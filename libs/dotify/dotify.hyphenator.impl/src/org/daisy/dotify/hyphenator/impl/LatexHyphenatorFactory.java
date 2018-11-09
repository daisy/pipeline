package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;

/**
 * Provides a hyphenator factory that uses latex hyphenation rules.
 * @author Joel Håkansson
 */
public class LatexHyphenatorFactory implements HyphenatorFactory {
	private final LatexHyphenatorCore core;

	/**
	 * Constructs a new LatexHypenator to be used by a hyphenator factory.
	 * @param core the hyphenator core
	 */
	public LatexHyphenatorFactory(LatexHyphenatorCore core) {
		this.core = core;
	}

	@Override
	public HyphenatorInterface newHyphenator(String locale) throws HyphenatorConfigurationException {
		return new LatexHyphenator(core.getHyphenator(locale));
	}

	@Override
	public Object getFeature(String key) {
		return null;
	}

	@Override
	public void setFeature(String key, Object value) throws HyphenatorConfigurationException {
		throw new LatexHyphenatorConfigurationException();
	}

}