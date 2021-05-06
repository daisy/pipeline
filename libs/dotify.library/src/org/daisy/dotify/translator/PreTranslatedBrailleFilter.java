package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;

import java.util.stream.Collectors;

/**
 * Provides a braille filter for already translated braille.
 *
 * @author Joel Håkansson
 */
public class PreTranslatedBrailleFilter implements BrailleFilter {

    /**
     * Creates a new pre-translated braille filter.
     */
    public PreTranslatedBrailleFilter() {
        //nothing to initialize
    }

    @Override
    public String filter(Translatable specification) throws TranslationException {
        return specification.getText();
    }

    @Override
    public String filter(TranslatableWithContext specification) throws TranslationException {
        return specification.getTextToTranslate().stream()
                .map(v -> v.resolve())
                .collect(Collectors.joining());
    }

}
