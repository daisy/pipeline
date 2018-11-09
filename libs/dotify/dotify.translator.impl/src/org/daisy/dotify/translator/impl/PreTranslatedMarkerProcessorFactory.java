package org.daisy.dotify.translator.impl;

import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.MarkerProcessorFactory;
import org.daisy.dotify.translator.DefaultMarkerProcessor;

class PreTranslatedMarkerProcessorFactory implements
		MarkerProcessorFactory {
	//TODO: remove when this string is part of the api
	static final String PRE_TRANSLATED = "pre-translated";

	@Override
	public MarkerProcessor newMarkerProcessor(String locale, String mode) throws MarkerProcessorConfigurationException {
		if (PRE_TRANSLATED.equals(mode)) {
			return new DefaultMarkerProcessor.Builder().build();
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
