package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryService;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Provides a hyphenator factory service for latex hyphenation rules.
 *
 * @author Joel Håkansson
 */
@Component
public class JHyphenatorFactoryService implements HyphenatorFactoryService {
    private final List<String> locales;

    /**
     * Creates a new instance.
     */
    public JHyphenatorFactoryService() {
        locales = new ArrayList<>();

        String operSys = System.getProperty("os.name").toLowerCase();
        if (!operSys.contains("linux")) {
            return;
        }

        ResourceBundle p = PropertyResourceBundle.getBundle("org/daisy/dotify/hyphenator/impl/JHyphenator");
        Enumeration<String> keyEnum = p.getKeys();
        while (keyEnum.hasMoreElements()) {
            String key = keyEnum.nextElement();
            File file = new File("/usr/share/hyphen", p.getString(key));
            if (file.exists()) {
                locales.add(key);
            }
        }
    }

    @Override
    public boolean supportsLocale(String locale) {
        return locales.contains(locale);
    }

    @Override
    public HyphenatorFactory newFactory() {
        return new JHyphenatorFactory();
    }

    @Override
    public Collection<String> listLocales() {
        return locales;
    }

}
