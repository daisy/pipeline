package org.daisy.dotify.translator.impl;

import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderFactoryService;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a braille text border factory service.
 * @author Joel Håkansson
 */
@Component
public class BrailleTextBorderFactoryService implements
		TextBorderFactoryService {

	@Override
	public TextBorderFactory newFactory() {
		return new BrailleTextBorderFactory();
	}

	@Override
	public void setCreatedWithSPI() {
	}

}
