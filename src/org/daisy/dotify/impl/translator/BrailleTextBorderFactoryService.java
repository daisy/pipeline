package org.daisy.dotify.impl.translator;

import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderFactoryService;

import aQute.bnd.annotation.component.Component;

@Component
public class BrailleTextBorderFactoryService implements
		TextBorderFactoryService {

	public TextBorderFactory newFactory() {
		return new BrailleTextBorderFactory();
	}

}
