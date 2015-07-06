package org.daisy.dotify.impl.input.xml;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.cr.TaskGroup;
import org.daisy.dotify.api.cr.TaskGroupFactory;
import org.daisy.dotify.api.cr.TaskGroupSpecification;
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
public class XMLInputManagerFactory implements TaskGroupFactory {
	private final Whatever locator;

	private final Set<TaskGroupSpecification> supportedSpecifications;
	
	public XMLInputManagerFactory() {
		this.locator = new Whatever();
		DefaultInputUrlResourceLocator p = DefaultInputUrlResourceLocator.getInstance();
		Set<String> supportedFormats = p.listFileFormats();
		supportedFormats.add("xml");
		supportedSpecifications = new HashSet<TaskGroupSpecification>();
		for (String format : supportedFormats) {
			for (String locale : locator.listSupportedLocales()) {
				supportedSpecifications.add(new TaskGroupSpecification(format, "obfl", locale));
			}
		}
	}

	@Override
	public boolean supportsSpecification(TaskGroupSpecification spec) {
		return supportedSpecifications.contains(spec);
	}
	
	@Override
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(supportedSpecifications);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
        return new XMLInputManager(locator.getResourceLocator(spec.getLocale()), new CommonResourceLocator("resource-files/common"));
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
