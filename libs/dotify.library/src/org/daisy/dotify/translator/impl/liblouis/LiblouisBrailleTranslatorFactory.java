package org.daisy.dotify.translator.impl.liblouis;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.translator.SimpleBrailleTranslator;
import org.daisy.dotify.translator.impl.ConfigurableBrailleFinalizer;

import java.util.Objects;

class LiblouisBrailleTranslatorFactory implements BrailleTranslatorFactory {
    private final HyphenatorFactoryMakerService hyphenatorService;

    LiblouisBrailleTranslatorFactory(HyphenatorFactoryMakerService hyphenatorService) {
        this.hyphenatorService = Objects.requireNonNull(hyphenatorService);
    }

    @Override
    public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException {
        LiblouisBrailleFilter lbf = new LiblouisBrailleFilter(
            new TranslatorSpecification(locale, mode),
            LiblouisMarkerProcessor.newInstance(),
            hyphenatorService
        );
        return new SimpleBrailleTranslator(lbf, new ConfigurableBrailleFinalizer.Builder().build(), mode);
    }

}
