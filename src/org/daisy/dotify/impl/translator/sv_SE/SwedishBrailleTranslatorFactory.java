package org.daisy.dotify.impl.translator.sv_SE;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.translator.DefaultBrailleFilter;
import org.daisy.dotify.translator.SimpleBrailleTranslator;

class SwedishBrailleTranslatorFactory implements BrailleTranslatorFactory {
	private final static String sv_SE = "sv-SE";
	private final HyphenatorFactoryMakerService hyphenatorService;

	public SwedishBrailleTranslatorFactory(HyphenatorFactoryMakerService hyphenatorService) {
		this.hyphenatorService = hyphenatorService;
	}

	public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException {
		if (hyphenatorService == null) {
			throw new SwedishTranslatorConfigurationException("HyphenatorFactoryMakerService not set.");
		} else if (sv_SE.equalsIgnoreCase(locale) && mode.equals(MODE_UNCONTRACTED)) {

			MarkerProcessor sap;
			try {
				sap = new SwedishMarkerProcessorFactory().newMarkerProcessor(locale, mode);
			} catch (MarkerProcessorConfigurationException e) {
				throw new SwedishTranslatorConfigurationException(e);
			}

			return new SimpleBrailleTranslator(
					new DefaultBrailleFilter(new SwedishBrailleFilter(), sv_SE, sap, hyphenatorService),
					new SwedishBrailleFinalizer(), sv_SE, mode);
		} 
		throw new SwedishTranslatorConfigurationException("Factory does not support " + locale + "/" + mode);
	}
	
	private class SwedishTranslatorConfigurationException extends TranslatorConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5954729812690753410L;

		public SwedishTranslatorConfigurationException(String message) {
			super(message);
		}

		SwedishTranslatorConfigurationException(Throwable cause) {
			super(cause);
		}
		
	}

}
