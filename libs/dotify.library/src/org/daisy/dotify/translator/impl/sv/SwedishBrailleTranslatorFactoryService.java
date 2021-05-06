package org.daisy.dotify.translator.impl.sv;

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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides a Swedish braille translator factory service.
 *
 * @author Joel Håkansson
 */
@Component
public class SwedishBrailleTranslatorFactoryService implements
        BrailleTranslatorFactoryService {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
        SwedishBrailleTranslatorFactoryService.class.getPackage().getName() + ".messages",
        Locale.getDefault()
    );
    private HyphenatorFactoryMakerService hyphenator = null;
    private final List<TranslatorSpecification> specs;

    /**
     * Creates a new Swedish braille translator factory service.
     */
    public SwedishBrailleTranslatorFactoryService() {
        this.specs = new ArrayList<>();
        String uncontracted = RESOURCE_BUNDLE.getString("uncontracted-6-dot");
        String preTranslated = RESOURCE_BUNDLE.getString("pre-translated");
        String descUncontracted = RESOURCE_BUNDLE.getString("uncontracted-description");
        String descPreTranslated = RESOURCE_BUNDLE.getString("pre-translated-description");
        specs.add(new TranslatorSpecification("sv", TranslatorMode.Builder
                .withType(TranslatorType.UNCONTRACTED)
                .displayName(uncontracted)
                .description(descUncontracted)
                .build()));
        specs.add(new TranslatorSpecification("sv", TranslatorMode.Builder
                .withType(TranslatorType.PRE_TRANSLATED)
                .displayName(preTranslated)
                .description(descPreTranslated)
                .build()));
        specs.add(new TranslatorSpecification("sv-SE", TranslatorMode.Builder
                .withType(TranslatorType.UNCONTRACTED)
                .displayName(uncontracted)
                .description(descUncontracted)
                .build()));
        specs.add(new TranslatorSpecification("sv-SE", TranslatorMode.Builder
                .withType(TranslatorType.PRE_TRANSLATED)
                .displayName(preTranslated)
                .description(descPreTranslated)
                .build()));
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
    public BrailleTranslatorFactory newFactory() {
        return new SwedishBrailleTranslatorFactory(hyphenator);
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
