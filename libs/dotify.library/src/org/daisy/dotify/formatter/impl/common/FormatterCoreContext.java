package org.daisy.dotify.formatter.impl.common;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Write java doc.
 */
public class FormatterCoreContext {
    private final TextBorderFactoryMakerService tbf;
    private final FormatterConfiguration config;
    private final BrailleTranslator translator;
    private final BrailleTranslatorFactoryMakerService translatorFactory;
    private final char spaceChar;
    private final Map<String, BrailleTranslator> cache;

    protected FormatterCoreContext(
        BrailleTranslatorFactoryMakerService translatorFactory,
        TextBorderFactoryMakerService tbf,
        FormatterConfiguration config
    ) {
        this.tbf = tbf;
        this.config = config;
        this.translatorFactory = translatorFactory;
        this.cache = new HashMap<>();
        try {
            this.translator = translatorFactory.newTranslator(config.getLocale(), config.getTranslationMode());
            cache.put(config.getTranslationMode(), translator);
        } catch (TranslatorConfigurationException e) {
            throw new IllegalArgumentException(e);
        }

        //margin char can only be a single character, the reason for going through the translator
        //is because output isn't always braille.
        try {
            this.spaceChar = getDefaultTranslator().translate(
                    Translatable.text(" ").build()
            ).getTranslatedRemainder().charAt(0);
        } catch (TranslationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTranslatorMode() {
        return config.getTranslationMode();
    }

    public BrailleTranslator getDefaultTranslator() {
        return translator;
    }

    public BrailleTranslator getTranslator(String mode) {
        if (mode == null) {
            return translator;
        }
        BrailleTranslator ret = cache.get(mode);
        if (ret == null) {
            try {
                ret = translatorFactory.newTranslator(getConfiguration().getLocale(), mode);
            } catch (TranslatorConfigurationException e) {
                throw new IllegalArgumentException(e);
            }
            cache.put(mode, ret);
        }
        return ret;
    }

    public TextBorderFactoryMakerService getTextBorderFactoryMakerService() {
        return tbf;
    }

    public FormatterConfiguration getConfiguration() {
        return config;
    }

    public char getSpaceCharacter() {
        return spaceChar;
    }

}
