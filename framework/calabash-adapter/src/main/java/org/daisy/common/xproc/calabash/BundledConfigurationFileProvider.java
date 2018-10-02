package org.daisy.common.xproc.calabash;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

public class BundledConfigurationFileProvider implements ConfigurationFileProvider {
	
	private static final String PATH = "path";
	
	private URL configFile = null;
	private String name = null;
	
	protected void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		name = properties.get("component.name").toString();
		String path = properties.get(PATH).toString();
		if (path == null)
			path = "/config-calabash.xml";
		configFile = context.getBundleContext().getBundle().getEntry(path);
		if (configFile == null)
			throw new IllegalArgumentException("Calabash configuration file at location " + path + " could not be found");
	}
	
	public InputStream get() {
		if (configFile == null)
			throw new IllegalStateException();
		try {
			return configFile.openStream();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
}
