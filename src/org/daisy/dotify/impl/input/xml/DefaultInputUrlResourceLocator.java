package org.daisy.dotify.impl.input.xml;

import java.io.IOException;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

import org.daisy.dotify.common.io.AbstractResourceLocator;
import org.daisy.dotify.common.io.ResourceLocatorException;

class DefaultInputUrlResourceLocator extends AbstractResourceLocator {
	private static DefaultInputUrlResourceLocator instance;
	private Properties props;

	private DefaultInputUrlResourceLocator(String basePath) {
		super(basePath);
		props = null;
	}

	synchronized static DefaultInputUrlResourceLocator getInstance() {
		if (instance==null) {
			instance = new DefaultInputUrlResourceLocator("resource-files");
		}
		return instance;
	}
	
	private synchronized void loadIfNotLoaded() throws ResourceLocatorException {
		if (props==null) {
			props = new Properties();
			try {
				props.loadFromXML(getResource("input_format_catalog.xml").openStream());
			} catch (InvalidPropertiesFormatException e) {
				throw new ResourceLocatorException();
			} catch (IOException e) {
				throw new ResourceLocatorException();
			}
		}
	}

	Properties getInputFormatCatalog() throws ResourceLocatorException {
		loadIfNotLoaded();
		return props;
	}
	
	String getConfigFileName(String rootElement, String rootNS) throws ResourceLocatorException {
		loadIfNotLoaded();
		if (rootNS!=null) {
			return props.getProperty(rootElement+"@"+rootNS);
		} else {
			return props.getProperty(rootElement);
		}
	}

	Set<String> listFileFormats() {
		Set<String> ret = new HashSet<String>();
		try {
			loadIfNotLoaded();
		} catch (ResourceLocatorException e) {
			return ret;
		}
		for (Object o : props.keySet()) {
			String s = o.toString();
			int inx;
			if ((inx = s.indexOf('@')) > -1) {
				ret.add(s.substring(0, inx));
			} else {
				ret.add(s);
			}
		}
		return ret;
	}

}
