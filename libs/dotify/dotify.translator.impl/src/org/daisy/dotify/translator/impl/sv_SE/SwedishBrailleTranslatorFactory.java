package org.daisy.dotify.translator.impl.sv_SE;

import java.util.Optional;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.translator.DefaultBrailleFilter;
import org.daisy.dotify.translator.PreTranslatedBrailleFilter;
import org.daisy.dotify.translator.SimpleBrailleTranslator;

class SwedishBrailleTranslatorFactory implements BrailleTranslatorFactory {
	//TODO: remove when this string is part of the api
	static final String PRE_TRANSLATED = "pre-translated";
	private static final String sv = "sv";
	private static final String sv_SE = "sv-SE";
	private final HyphenatorFactoryMakerService hyphenatorService;

	public SwedishBrailleTranslatorFactory(HyphenatorFactoryMakerService hyphenatorService) {
		this.hyphenatorService = hyphenatorService;
	}

	@Override
	public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException {
		if (hyphenatorService == null) {
			throw new SwedishTranslatorConfigurationException("HyphenatorFactoryMakerService not set.");
		}
		Optional<String> loc = getSupportedLocale(locale);
		if (loc.isPresent() && mode.equals(MODE_UNCONTRACTED)) {

			MarkerProcessor sap;
			try {
				sap = new SwedishMarkerProcessorFactory().newMarkerProcessor(loc.get(), mode);
			} catch (MarkerProcessorConfigurationException e) {
				throw new SwedishTranslatorConfigurationException(e);
			}

			return new SimpleBrailleTranslator(
					new DefaultBrailleFilter(new SwedishBrailleFilter(loc.get()), loc.get(), sap, hyphenatorService),
					new SwedishBrailleFinalizer(), mode);
		} else if (loc.isPresent() && mode.equals(PRE_TRANSLATED)) {
			return new SimpleBrailleTranslator(
					new PreTranslatedBrailleFilter(),
					new SwedishBrailleFinalizer(),
					mode);
		}
		throw new SwedishTranslatorConfigurationException("Factory does not support " + locale + "/" + mode);
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
