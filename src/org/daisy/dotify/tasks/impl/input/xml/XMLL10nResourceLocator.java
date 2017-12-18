package org.daisy.dotify.tasks.impl.input.xml;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.common.io.AbstractResourceLocator;
import org.daisy.dotify.common.io.ResourceLocator;

/**
 * Provides a resource locator for xml localization data.
 * 
 * @author Joel Håkansson
 */
public enum XMLL10nResourceLocator {
	/**
	 * The instance 
	 */
	INSTANCE;
	private final Properties locales;
	
	private XMLL10nResourceLocator() {
		Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
		locales = new Properties();
		try {
			InputLocalizationResourceLocator loc = new InputLocalizationResourceLocator("resource-files");
	        URL tablesURL = loc.getResource("localization_catalog.xml");
	        if(tablesURL!=null){
	        	locales.loadFromXML(tablesURL.openStream());
	        } else {
	        	logger.warning("Cannot locate catalog file");
	        }
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load catalog.", e);
		}
	}
	
	/**
	 * Gets the singleton instance.
	 * @return returns the instance
	 */
	public static XMLL10nResourceLocator getInstance() {
		return INSTANCE;
	}

	/**
	 * Lists all supported locales, that is to say all locales that are
	 * in the localization catalog.
	 * @return returns a list of supported locales
	 */
	public Set<String> listSupportedLocales() {
		HashSet<String> ret = new HashSet<>();
		for (Object key : locales.keySet()) {
			ret.add(key.toString());
		}
		return ret;
	}
	
	/**
	 * Gets a resource locator for the given locale.
	 * @param locale the locale to get a resource locator for
	 * @return returns a resource locator
	 * @throws IllegalArgumentException if the locale is not supported
	 */
	public ResourceLocator getResourceLocator(String locale) {
		String languageFileRelativePath = locales.getProperty(locale);
        if(languageFileRelativePath==null) {
        	throw new IllegalArgumentException("Locale not supported: " + locale);
        } else {
        	return new InputLocalizationResourceLocator(languageFileRelativePath);
        }
	}
	
	/**
	 * Provides a resource locator for localized resources. Using this
	 * class makes it possible to locate a resource using a path relative
	 * to the localization base folder, without knowledge of the locale
	 * to base folder mapping.
	 * @author Joel Håkansson
	 */
	private class InputLocalizationResourceLocator extends AbstractResourceLocator {
		
		public InputLocalizationResourceLocator(String basePath) {
			super(basePath);
		}
		
	}
}