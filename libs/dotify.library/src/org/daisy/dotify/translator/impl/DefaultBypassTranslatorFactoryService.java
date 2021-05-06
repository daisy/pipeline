package org.daisy.dotify.translator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMaker;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryService;
import org.daisy.dotify.api.translator.TranslatorMode;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.api.translator.TranslatorType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides a pass through translator factory service.
 *
 * @author Joel Håkansson
 */
@Component
public class DefaultBypassTranslatorFactoryService implements
        BrailleTranslatorFactoryService {

    private HyphenatorFactoryMakerService hyphenator = null;

    @Override
    public boolean supportsSpecification(String locale, String mode) {
        return mode.equals(TranslatorType.BYPASS.toString());
    }

    @Override
    public BrailleTranslatorFactory newFactory() {
        return new DefaultBypassTranslatorFactory(hyphenator);
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
    public Collection<TranslatorSpecification> listSpecifications() {
        List<TranslatorSpecification> ret = new ArrayList<>();
        for (String loc : hyphenator.listLocales()) {
            ret.add(new TranslatorSpecification(loc, TranslatorMode.Builder.withType(TranslatorType.BYPASS)
                    .displayName("Hyphenator: " + loc)
                    .description("Identity translator that doesn't translate any characters.")
                    .build())
            );
        }
        return ret;
    }

    @Override
    public void setCreatedWithSPI() {
        setHyphenator(HyphenatorFactoryMaker.newInstance());
    }

}
