package org.daisy.dotify.translator.impl.liblouis;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMaker;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.BrailleFilterFactoryService;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a Liblouis braille filter factory service.
 *
 * @author Joel Håkansson
 */
@Component
public class LiblouisBrailleFilterFactoryService implements BrailleFilterFactoryService {
    private final List<TranslatorSpecification> specs;
    private HyphenatorFactoryMakerService hyphenator = null;

    /**
     * Creates a new Liblouis braille filter factory service.
     */
    public LiblouisBrailleFilterFactoryService() {
        this.specs = LiblouisSpecifications.getMap().keySet()
                .stream()
                .map(v -> (TranslatorSpecification) v)
                .collect(Collectors.toList());
    }

    @Override
    public boolean supportsSpecification(String locale, String mode) {
        TranslatorSpecification target = new TranslatorSpecification(locale, mode);
        for (TranslatorSpecification spec : specs) {
            if (target.equals(spec)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<TranslatorSpecification> listSpecifications() {
        return specs;
    }

    @Override
    public BrailleFilterFactory newFactory() {
        return new LiblouisBrailleFilterFactory(hyphenator);
    }

    /**
     * Sets the hyphenator factory maker service.
     *
     * @param hyphenator the hyphenator factory maker service.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setHyphenator(HyphenatorFactoryMakerService hyphenator) {
        this.hyphenator = hyphenator;
    }

    /**
     * Unsets the hyphenator factory maker service.
     *
     * @param hyphenator the instance to unset.
     */
    public void unsetHyphenator(HyphenatorFactoryMakerService hyphenator) {
        this.hyphenator = null;
    }

    @Override
    public void setCreatedWithSPI() {
        setHyphenator(HyphenatorFactoryMaker.newInstance());
    }

}
