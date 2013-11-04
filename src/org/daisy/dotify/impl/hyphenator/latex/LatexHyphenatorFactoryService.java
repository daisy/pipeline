package org.daisy.dotify.impl.hyphenator.latex;

import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryService;

import aQute.bnd.annotation.component.Component;

@Component
public class LatexHyphenatorFactoryService implements HyphenatorFactoryService {

	// FIXME: singleton use not checked for thread safety
	public boolean supportsLocale(String locale) {
		return LatexHyphenator.supportsLocale(locale);
	}

	public HyphenatorFactory newFactory() {
		return new LatexHyphenatorFactory();
	}

}
