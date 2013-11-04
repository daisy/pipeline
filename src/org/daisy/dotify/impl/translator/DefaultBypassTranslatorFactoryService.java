package org.daisy.dotify.impl.translator;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryService;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class DefaultBypassTranslatorFactoryService implements
		BrailleTranslatorFactoryService {

	private HyphenatorFactoryMakerService hyphenator = null;

	public boolean supportsSpecification(String locale, String mode) {
		return mode.equals(BrailleTranslatorFactory.MODE_BYPASS);
	}

	public BrailleTranslatorFactory newFactory() {
		return new DefaultBypassTranslatorFactory(hyphenator);
	}

	@Reference
	public void setHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = hyphenator;
	}

	public void unsetHyphenator(HyphenatorFactoryMakerService hyphenator) {
		this.hyphenator = null;
	}

}
