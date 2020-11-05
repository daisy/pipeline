package org.daisy.pipeline.braille.liblouis.impl;

import java.net.URL;
import java.net.URI;
import java.util.Hashtable;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.NativePath;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisExternalNativePath",
	service = { NativePath.class },
	property = {
		"identifier:String=http://www.liblouis.org/native/EXTERNAL"
	}
)
public class LiblouisExternalNativePath implements NativePath {
	
	final static boolean LIBLOUIS_EXTERNAL = Boolean.getBoolean("org.daisy.pipeline.braille.liblouis.external");
	
	/**
	 * @throws RuntimeException if using the Liblouis library present on the system is not allowed
	 */
	@Activate
	protected void activate() throws RuntimeException {
		if (!LIBLOUIS_EXTERNAL)
			throw new RuntimeException("Using external Liblouis is not allowed");
	}
	
	public URI getIdentifier() { return URLs.asURI("http://www.liblouis.org/native/EXTERNAL"); }
	public URI canonicalize(URI resource) { throw new UnsupportedOperationException(); }
	public URL resolve(URI resource) { throw new UnsupportedOperationException(); }
	public Iterable<URI> get(String query) { throw new UnsupportedOperationException(); }
	
	// Throwing an exception from the activate method, although permitted by the spec
	// (https://osgi.org/specification/osgi.cmpn/7.0.0/service.component.html#service.component-activate.method),
	// breaks bindings with cardinality 0..1 in Felix. For this reason the DS annotations are used
	// only to create the SPI wrapper, and for OSGi we use an activator.
	public static class Activator implements BundleActivator {
		
		public void start(BundleContext context) throws Exception {
			if (LIBLOUIS_EXTERNAL) {
				NativePath dummy = new LiblouisExternalNativePath();
				Hashtable<String,Object> properties = new Hashtable<String,Object>();
				properties.put("identifier", "http://www.liblouis.org/native/EXTERNAL");
				context.registerService(NativePath.class.getName(), dummy, properties);
			}
		}
		
		public void stop(BundleContext context) throws Exception {}
		
	}
}
