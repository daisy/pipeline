package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryService;
import org.osgi.service.component.annotations.Component;

import java.util.Collection;

/**
 * Provides a hyphenator factory service for latex hyphenation rules.
 *
 * @author Joel Håkansson
 */
@Component
public class LatexHyphenatorFactoryService implements HyphenatorFactoryService {
    private final LatexHyphenatorCore core;

    /**
     * Creates a new instance.
     */
    public LatexHyphenatorFactoryService() {
        this.core = LatexHyphenatorCore.getInstance();
    }

    @Override
    public boolean supportsLocale(String locale) {
        return core.supportsLocale(locale);
    }

    @Override
    public HyphenatorFactory newFactory() {
        return new LatexHyphenatorFactory(core);
    }

    @Override
    public Collection<String> listLocales() {
        return core.listLocales();
    }

}
