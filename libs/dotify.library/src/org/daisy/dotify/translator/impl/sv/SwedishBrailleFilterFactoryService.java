package org.daisy.dotify.translator.impl.sv;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMaker;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.BrailleFilterFactoryService;
import org.daisy.dotify.api.translator.TranslatorMode;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.api.translator.TranslatorType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides a Swedish braille filter factory service.
 *
 * @author Joel Håkansson
 */
@Component
public class SwedishBrailleFilterFactoryService implements
        BrailleFilterFactoryService {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
        SwedishBrailleFilterFactoryService.class.getPackage().getName() + ".messages",
        Locale.getDefault()
    );
    private HyphenatorFactoryMakerService hyphenator = null;
    private final List<TranslatorSpecification> specs;

    /**
     * Creates a new Swedish braille filter factory service.
     */
    public SwedishBrailleFilterFactoryService() {
        this.specs = new ArrayList<>();
        String displayName = RESOURCE_BUNDLE.getString("uncontracted-6-dot");
        String desc = RESOURCE_BUNDLE.getString("uncontracted-description");
        specs.add(new TranslatorSpecification("sv", TranslatorMode.Builder
                .withType(TranslatorType.UNCONTRACTED)
                .displayName(displayName)
                .description(desc)
                .build()));
        specs.add(new TranslatorSpecification("sv-SE", TranslatorMode.Builder
                .withType(TranslatorType.UNCONTRACTED)
                .displayName(displayName)
                .description(desc)
                .build()));
    }

    @Override
    public boolean supportsSpecification(String locale, String mode) {
        return (
            "sv".equalsIgnoreCase(locale) ||
            "sv-SE".equalsIgnoreCase(locale)
        ) &&
        mode.equals(TranslatorType.UNCONTRACTED.toString());
    }

    @Override
    public BrailleFilterFactory newFactory() {
        return new SwedishBrailleFilterFactory(hyphenator);
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
        return specs;
    }

    @Override
    public void setCreatedWithSPI() {
        setHyphenator(HyphenatorFactoryMaker.newInstance());
    }

}
