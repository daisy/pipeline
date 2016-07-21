package org.daisy.pipeline.braille.liblouis.impl;

import java.net.URI;
import java.net.URL;
import java.util.Hashtable;

import org.daisy.pipeline.braille.common.NativePath;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	private final static boolean LIBLOUIS_EXTERNAL = Boolean.getBoolean("org.daisy.pipeline.liblouis.external");
	
	public void start(BundleContext context) throws Exception {
		if (LIBLOUIS_EXTERNAL) {
			NativePath dummy = new NativePath() {
				public URI getIdentifier() { return asURI("http://www.liblouis.org/native/EXTERNAL"); }
				public URI canonicalize(URI resource) { return null; }
				public URL resolve(URI resource) { return null; }
				public Iterable<URI> get(String query) { return null; }
			};
			Hashtable<String,Object> properties = new Hashtable<String,Object>();
			properties.put("identifier", "http://www.liblouis.org/native/EXTERNAL");
			context.registerService(NativePath.class.getName(), dummy, properties);
		}
	}
	
	public void stop(BundleContext context) throws Exception {}
	
}
