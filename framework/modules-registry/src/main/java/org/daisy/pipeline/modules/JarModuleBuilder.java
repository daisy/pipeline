package org.daisy.pipeline.modules;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.daisy.pipeline.modules.ResourceLoader;

public class JarModuleBuilder extends AbstractModuleBuilder<JarModuleBuilder> {
	
	public JarModuleBuilder withJarFile(final File jarFile) {
		withLoader(new ResourceLoader() {
			
			// Can't use ClassLoader.getResource() because there can be name
			// clashes between resources in different JARs. Alternative
			// solution would be to have a ClassLoader for each JAR.
			@Override
			@SuppressWarnings(
				"deprecation" // URLDecode.decode is deprecated
			)
			public URL loadResource(String path) {
				// Paths are assumed to be relative to META-INF
				if (!path.startsWith("../")) {
					throw new RuntimeException("Paths must start with '../' but got '" + path + "'");
				}
				path = path.substring(2);
				try {
					return jarFile.isDirectory() ?
						new URL(         URLDecoder.decode((jarFile.toURI().toASCIIString() + path).replace("+", "%2B"))) :
						new URL("jar:" + URLDecoder.decode((jarFile.toURI().toASCIIString() + "!" + path).replace("+", "%2B")));
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public Iterable<URL> loadResources(final String path) {
				throw new UnsupportedOperationException("Not supported without OSGi.");
			}
		});
		return this;
	}
	
	public JarModuleBuilder self() {
		return this;
	}
}
