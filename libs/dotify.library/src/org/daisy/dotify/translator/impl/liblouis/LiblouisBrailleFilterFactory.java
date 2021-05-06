package org.daisy.dotify.translator.impl.liblouis;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;

import java.util.Objects;

class LiblouisBrailleFilterFactory implements BrailleFilterFactory {
    private final HyphenatorFactoryMakerService hyphenatorService;

    LiblouisBrailleFilterFactory(HyphenatorFactoryMakerService hyphenatorService) {
        this.hyphenatorService = Objects.requireNonNull(hyphenatorService);
    }

    @Override
    public BrailleFilter newFilter(String locale, String mode) throws TranslatorConfigurationException {
        return new LiblouisBrailleFilter(
            new TranslatorSpecification(locale, mode),
            LiblouisMarkerProcessor.newInstance(),
            hyphenatorService
        );
    }

}
