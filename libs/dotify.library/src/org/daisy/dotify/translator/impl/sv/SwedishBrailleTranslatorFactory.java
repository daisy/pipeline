package org.daisy.dotify.translator.impl.sv;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.translator.DefaultBrailleFilter;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.PreTranslatedBrailleFilter;
import org.daisy.dotify.translator.SimpleBrailleTranslator;
import org.daisy.dotify.translator.impl.DefaultBrailleFinalizer;
import org.daisy.dotify.translator.impl.sv.SwedishMarkerProcessorFactory.SwedishMarkerProcessorConfigurationException;

import java.util.Optional;

class SwedishBrailleTranslatorFactory implements BrailleTranslatorFactory {
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
        if (loc.isPresent() && mode.equals(TranslatorType.UNCONTRACTED.toString())) {

            DefaultMarkerProcessor sap;
            try {
                sap = new SwedishMarkerProcessorFactory().newMarkerProcessor(loc.get(), mode);
            } catch (SwedishMarkerProcessorConfigurationException e) {
                throw new SwedishTranslatorConfigurationException(e);
            }

            return new SimpleBrailleTranslator(
                    new DefaultBrailleFilter(new SwedishBrailleFilter(loc.get()), loc.get(), sap, hyphenatorService),
                    new DefaultBrailleFinalizer(), mode);
        } else if (loc.isPresent() && mode.equals(TranslatorType.PRE_TRANSLATED.toString())) {
            return new SimpleBrailleTranslator(
                    new PreTranslatedBrailleFilter(),
                    new DefaultBrailleFinalizer(),
                    mode);
        }
        throw new SwedishTranslatorConfigurationException("Factory does not support " + locale + "/" + mode);
    }

    /**
     * Verifies that the given locale is supported.
     *
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
