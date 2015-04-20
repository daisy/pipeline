package org.daisy.dotify.impl.translator;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.MarkerStyleConstants;
import org.daisy.dotify.translator.SimpleMarkerDictionary;

class DefaultBypassMarkerProcessorFactory implements
		MarkerProcessorFactory {

	public MarkerProcessor newMarkerProcessor(String locale, String mode) throws MarkerProcessorConfigurationException {
		if (mode.equals(BrailleTranslatorFactory.MODE_BYPASS)) {
			SimpleMarkerDictionary dd = new SimpleMarkerDictionary(new Marker("* ", ""));

			DefaultMarkerProcessor sap = new DefaultMarkerProcessor.Builder().addDictionary(MarkerStyleConstants.DD, dd).build();
			return sap;
		}
		throw new DefaultBypassMarkerProcessorConfigurationException("Factory does not support " + locale + "/" + mode);
	}

	private class DefaultBypassMarkerProcessorConfigurationException extends MarkerProcessorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7831296813639319600L;

		private DefaultBypassMarkerProcessorConfigurationException(String message) {
			super(message);
		}
		
	}

}
