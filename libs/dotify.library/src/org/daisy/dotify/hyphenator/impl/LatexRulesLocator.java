package org.daisy.dotify.hyphenator.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a resource locator for latex rule files.
 *
 * @author Joel Håkansson
 */
public class LatexRulesLocator {
    private final Logger logger;
    private final Properties locales = new Properties();

    /**
     * Creates a new instance.
     */
    public LatexRulesLocator() {
        logger = Logger.getLogger(this.getClass().getCanonicalName());
        try {
            URL localesURL = getCatalogResourceURL();
            if (localesURL != null) {
                locales.loadFromXML(localesURL.openStream());
            } else {
                logger.warning("Cannot locate hyphenation locales");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load table list.", e);
        }
    }

    private URL getCatalogResourceURL() throws FileNotFoundException {
        return getResource("hyphenation-catalog.xml");
    }

    /**
     * Returns true if the specified locale is supported.
     *
     * @param locale the locale to test
     * @return returns true if the locale is supported, false otherwise
     */
    public boolean supportsLocale(String locale) {
        return locales.getProperty(locale) != null;
    }

    /**
     * Lists supported locales.
     *
     * @return returns a collection of locales
     */
    public Collection<?> listLocales() {
        return locales.keySet();
    }

    /**
     * Returns the properties for the specified locale.
     *
     * @param locale the locale
     * @return returns the properties, or null if not found.
     */
    public Properties getProperties(String locale) {
        String languageFileRelativePath = locales.getProperty(locale);
        if (languageFileRelativePath == null) {
            return null;
        } else {
            Properties p = new Properties();
            try {
                p.loadFromXML(getResource(languageFileRelativePath).openStream());
                return p;
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to read properties file: " + languageFileRelativePath, e);
            }
            return null;
        }
    }

    /**
     * Gets a resource with the specified relative path.
     *
     * @param path the path
     * @return returns an URL
     * @throws FileNotFoundException if the resource cannot be located
     */
    public URL getResource(String path) throws FileNotFoundException {
        URL url;
        url = this.getClass().getResource(path);
        if (null == url) {
            String qualifiedPath = this.getClass().getPackage().getName().replace('.', '/') + "/";
            url = this.getClass().getClassLoader().getResource(qualifiedPath + path);
        }
        if (url == null) {
            throw new FileNotFoundException(
                "Cannot find resource path '" + path + "' relative to " + this.getClass().getCanonicalName()
            );
        }
        return url;
    }
}
