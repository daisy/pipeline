package org.daisy.dotify.impl.translator;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryService;

import aQute.bnd.annotation.component.Component;

@Component
public class DefaultBypassMarkerProcessorFactoryService implements
		MarkerProcessorFactoryService {

	public boolean supportsSpecification(String locale, String mode) {
		return mode.equals(BrailleTranslatorFactory.MODE_BYPASS);
	}

	public MarkerProcessorFactory newFactory() {
		return new DefaultBypassMarkerProcessorFactory();
	}

}
