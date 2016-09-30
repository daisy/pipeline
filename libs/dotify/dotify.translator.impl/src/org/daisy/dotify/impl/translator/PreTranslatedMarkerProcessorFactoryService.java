package org.daisy.dotify.impl.translator;

import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryService;

import aQute.bnd.annotation.component.Component;

@Component
public class PreTranslatedMarkerProcessorFactoryService implements
		MarkerProcessorFactoryService {

	@Override
	public boolean supportsSpecification(String locale, String mode) {
		return mode.equals(PreTranslatedMarkerProcessorFactory.PRE_TRANSLATED);
	}

	@Override
	public MarkerProcessorFactory newFactory() {
		return new PreTranslatedMarkerProcessorFactory();
	}

	@Override
	public void setCreatedWithSPI() {
	}

}
