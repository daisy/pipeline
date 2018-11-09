package org.daisy.dotify.translator.impl.sv_SE;

import java.util.Optional;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.translator.DefaultBrailleFilter;

class SwedishBrailleFilterFactory implements BrailleFilterFactory {
	private static final String sv = "sv";
	private static final String sv_SE = "sv-SE";
	private final HyphenatorFactoryMakerService hyphenatorService;

	public SwedishBrailleFilterFactory(HyphenatorFactoryMakerService hyphenatorService) {
		this.hyphenatorService = hyphenatorService;
	}

	@Override
	public BrailleFilter newFilter(String locale, String mode) throws TranslatorConfigurationException {
		if (hyphenatorService == null) {
			throw new SwedishFilterConfigurationException("HyphenatorFactoryMakerService not set.");
		}
		Optional<String> loc = getSupportedLocale(locale);
		if (loc.isPresent() && mode.equals(BrailleTranslatorFactory.MODE_UNCONTRACTED)) {

			MarkerProcessor sap;
			try {
				sap = new SwedishMarkerProcessorFactory().newMarkerProcessor(loc.get(), mode);
			} catch (MarkerProcessorConfigurationException e) {
				throw new SwedishFilterConfigurationException(e);
			}
			return new DefaultBrailleFilter(new SwedishBrailleFilter(loc.get(), true), loc.get(), sap, hyphenatorService);
		} 
		throw new SwedishFilterConfigurationException("Factory does not support " + locale + "/" + mode);
	}
	
	/**
	 * Verifies that the given locale is supported.
	 * @param locale the locale
	 * @return the locale with correct case, or an empty optional if the locale is not supported
	 */
	private static Optional<String> getSupportedLocale(String locale) {
		if (sv.equalsIgnoreCase(locale)) {
			return Optional.of(sv);
		} else if (sv_SE.equalsIgnoreCase(locale)) {
			return Optional.of(sv_SE);
		} else {
			return Optional.empty();
		}
	}
	
	private static class SwedishFilterConfigurationException extends TranslatorConfigurationException {

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
