package org.daisy.dotify.impl.hyphenator.latex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LatexRulesLocator {
	private final Logger logger;
	private final Properties locales = new Properties();
	
	public LatexRulesLocator() {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		try {
	        URL localesURL = getCatalogResourceURL();
	        if(localesURL!=null){
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
	
	public boolean supportsLocale(String locale) {
		return locales.getProperty(locale) != null;
	}
	
	public Properties getProperties(String locale) {
		String languageFileRelativePath = locales.getProperty(locale);
        if(languageFileRelativePath==null) {
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

	public URL getResource(String path) throws FileNotFoundException {
		URL url;
	    url = this.getClass().getResource(path);
	    if(null==url) {
	    	String qualifiedPath = this.getClass().getPackage().getName().replace('.','/') + "/";	    	
	    	url = this.getClass().getClassLoader().getResource(qualifiedPath+path);
	    }
	    if(url==null) throw new FileNotFoundException("Cannot find resource path '" + path + "' relative to " + this.getClass().getCanonicalName());
	    return url;
	}
}
