package org.daisy.dotify.translator.impl.sv_SE;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryService;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a Swedish marker processor factory service.
 * @author Joel Håkansson
 *
 */
@Component
public class SwedishMarkerProcessorFactoryService implements
		MarkerProcessorFactoryService {

	@Override
	public boolean supportsSpecification(String locale, String mode) {
		return ("sv".equalsIgnoreCase(locale) || "sv-SE".equalsIgnoreCase(locale)) && mode.equals(BrailleTranslatorFactory.MODE_UNCONTRACTED);
	}

	@Override
	public MarkerProcessorFactory newFactory() {
		return new SwedishMarkerProcessorFactory();
	}

	@Override
	public void setCreatedWithSPI() {
	}

}
