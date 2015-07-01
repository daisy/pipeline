package org.daisy.dotify.impl.input.xml;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.cr.InputManager;
import org.daisy.dotify.api.cr.InputManagerFactory;
import org.daisy.dotify.common.io.AbstractResourceLocator;
import org.daisy.dotify.common.io.ResourceLocator;

import aQute.bnd.annotation.component.Component;

/**
 * Provides an XML input manager factory. This can be used when implementing
 * XML-formats in this package. It is specifically designed to inject the 
 * correct validation rules and XSLT stylesheet for any XML-format and locale
 * combination into the task chain. See the package documentation for information
 * on how to extend it.
 * 
 * @author Joel Håkansson
 *
 */
@Component
public class XMLInputManagerFactory implements InputManagerFactory {
	private final Whatever locator;
	private final Set<String> supportedFormats;
	
	public XMLInputManagerFactory() {
		this.locator = new Whatever();
		DefaultInputUrlResourceLocator p = DefaultInputUrlResourceLocator.getInstance();
		supportedFormats = p.listFileFormats();
		supportedFormats.add("xml");
	}

	public boolean supportsSpecification(String locale, String fileFormat) {
		return locator.supportsLocale(locale) && supportedFormats.contains(fileFormat);
	}
	
	public Set<String> listSupportedLocales() {
		return locator.listSupportedLocales();
	}

	public Set<String> listSupportedFileFormats() {
		return supportedFormats;
	}

	public InputManager newInputManager(String locale, String fileFormat) {
        return new XMLInputManager(locator.getResourceLocator(locale), new CommonResourceLocator("resource-files/common"));
	}

	private class CommonResourceLocator extends AbstractResourceLocator {
		CommonResourceLocator(String subpath) {
			super(subpath);
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
		
		private InputLocalizationResourceLocator(String basePath) {
			super(basePath);
		}
		
	}
	private class Whatever {
		private final Properties locales;
		
		Whatever() {
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
		 * Returns true if the specified locale is at all supported, that is to say
		 * that there is an entry for the locale in the localization catalog.
		 * @param locale the locale to test
		 * @return returns true if the locale is supported, false otherwise
		 */
		public boolean supportsLocale(String locale) {
			return locales.getProperty(locale)!=null;
		}
		
		/**
		 * Lists all supported locales, that is to say all locales that are
		 * in the localization catalog.
		 * @return returns a list of supported locales
		 */
		public Set<String> listSupportedLocales() {
			HashSet<String> ret = new HashSet<String>();
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
	}
}
