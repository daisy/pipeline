package org.daisy.dotify.impl.translator;

import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderFactoryService;

public class BrailleTextBorderFactoryService implements
		TextBorderFactoryService {

	public TextBorderFactory newFactory() {
		return new BrailleTextBorderFactory();
	}

}
