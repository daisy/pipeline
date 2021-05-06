package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.formatter.impl.core.FormatterContext;

/**
 * TODO: Write java doc.
 */
public class LazyFormatterContext {
    private final BrailleTranslatorFactoryMakerService translatorFactory;
    private final TextBorderFactoryMakerService tbf;
    private FormatterContext context = null;
    private FormatterConfiguration config = null;

    public LazyFormatterContext(
        BrailleTranslatorFactoryMakerService translatorFactory,
        TextBorderFactoryMakerService tbf,
        FormatterConfiguration config
    ) {
        if (config == null) {
            throw new IllegalArgumentException();
        }
        this.translatorFactory = translatorFactory;
        this.tbf = tbf;
        this.config = config;
    }

    public synchronized FormatterContext getFormatterContext() {
        if (context == null) {
            context = new FormatterContext(translatorFactory, tbf, config);
        }
        return context;
    }

    public void setConfiguration(FormatterConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException();
        }
        context = null;
        this.config = config;
    }

}
