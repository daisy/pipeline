package org.daisy.dotify.impl.translator.sv_SE;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryService;

public class SwedishMarkerProcessorFactoryService implements
		MarkerProcessorFactoryService {

	public boolean supportsSpecification(String locale, String mode) {
		return "sv-SE".equalsIgnoreCase(locale) && mode.equals(BrailleTranslatorFactory.MODE_UNCONTRACTED);
	}

	public MarkerProcessorFactory newFactory() {
		return new SwedishMarkerProcessorFactory();
	}

}
