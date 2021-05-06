package org.daisy.dotify.text.impl;

import org.daisy.dotify.api.text.Integer2TextFactory;
import org.daisy.dotify.api.text.Integer2TextFactoryService;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides a Swedish integer to text implementation.
 *
 * @author Joel Håkansson
 */
@Component
public class SwedishInteger2TextFactoryService implements
        Integer2TextFactoryService {

    private static final List<String> locales;

    static {
        locales = new ArrayList<>();
        locales.add("sv-SE");
        locales.add("sv");
    }

    @Override
    public boolean supportsLocale(String locale) {
        for (String l : locales) {
            if (l.equalsIgnoreCase(locale)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer2TextFactory newFactory() {
        return new SwedishInteger2TextFactory();
    }

    @Override
    public Collection<String> listLocales() {
        return locales;
    }

}
