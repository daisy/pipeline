package org.daisy.dotify.impl.hyphenator.latex;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

public class CWHyphenatorCore {
	private static CWHyphenatorCore instance;
	private final Properties tables;
	private final Map<String, CWHyphenatorAtom> map;
	private final Logger logger;
	
	private CWHyphenatorCore() {
		tables = loadProperties("compound-catalog.xml");
		map = new HashMap<String, CWHyphenatorAtom>();
		logger = Logger.getLogger(this.getClass().getCanonicalName());
	}
	
	public boolean supportsLocale(String locale) {
		return tables.getProperty(locale) != null;
	}
	
	public synchronized static CWHyphenatorCore getInstance() {
		if (instance==null) {
			instance = new CWHyphenatorCore();
		}
		return instance;
	}
	
	private Properties loadProperties(String path) {
		Properties ret = new Properties();
		try {
	        URL propertiesURL = this.getClass().getResource(path);
	        if (propertiesURL!=null){
	        	ret.loadFromXML(propertiesURL.openStream());
	        } else {
	        	logger.warning("Cannot locate properties file: " + path);
	        }
		} catch (IOException e) {
			logger.warning("Failed to load properties file: " + path);
		}
		return ret;
	}
	
	public CWHyphenatorAtom getHyphenator(String locale) throws HyphenatorConfigurationException {
		CWHyphenatorAtom hyph = map.get(locale);
		if (hyph==null) {
			String subPath = tables.getProperty(locale);
			if (subPath==null) {
				throw new LatexHyphenatorConfigurationException("No definition for locale: " + locale);
			}
			logger.fine("Loading hyphenation definition: " + subPath);
    		hyph = new CWHyphenatorAtom(subPath, locale);
    		map.put(locale, hyph);
		}
		return hyph;
	}

}
