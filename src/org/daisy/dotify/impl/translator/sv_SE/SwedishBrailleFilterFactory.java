package org.daisy.dotify.impl.translator.sv_SE;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.translator.DefaultBrailleFilter;

class SwedishBrailleFilterFactory implements BrailleFilterFactory {
	private final static String sv_SE = "sv-SE";
	private final HyphenatorFactoryMakerService hyphenatorService;

	public SwedishBrailleFilterFactory(HyphenatorFactoryMakerService hyphenatorService) {
		this.hyphenatorService = hyphenatorService;
	}

	public BrailleFilter newFilter(String locale, String mode) throws TranslatorConfigurationException {
		if (hyphenatorService == null) {
			throw new SwedishFilterConfigurationException("HyphenatorFactoryMakerService not set.");
		} else if (sv_SE.equalsIgnoreCase(locale) && mode.equals(BrailleTranslatorFactory.MODE_UNCONTRACTED)) {

			MarkerProcessor sap;
			try {
				sap = new SwedishMarkerProcessorFactory().newMarkerProcessor(locale, mode);
			} catch (MarkerProcessorConfigurationException e) {
				throw new SwedishFilterConfigurationException(e);
			}

			return new DefaultBrailleFilter(new SwedishBrailleFilter(), sv_SE, sap, hyphenatorService);
		} 
		throw new SwedishFilterConfigurationException("Factory does not support " + locale + "/" + mode);
	}
	
	private class SwedishFilterConfigurationException extends TranslatorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5954729812690753410L;

		public SwedishFilterConfigurationException(String message) {
			super(message);
		}

		SwedishFilterConfigurationException(Throwable cause) {
			super(cause);
		}
		
	}

}
