package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.daisy.common.spi.ServiceLoader;

import org.daisy.maven.xproc.calabash.Calabash;

import org.daisy.pipeline.modules.impl.resolver.ModuleUriResolver;

import static org.daisy.pipeline.maven.plugin.utils.URIs.asURI;

import org.xml.sax.InputSource;

class CalabashWithPipelineModules extends Calabash {
	
	CalabashWithPipelineModules(Collection<String> classPath) {
		super();
		final URIResolver resolver = getModuleUriResolver(classPath);
		setURIResolver(new URIResolver() {
			public Source resolve(String href, String base) throws TransformerException {
				Source source = resolver.resolve(href, base);
				if (source == null) {
					try {
						URI uri = (base != null) ?
							asURI(base).resolve(asURI(href)) :
							asURI(href);
						source = new SAXSource(new InputSource(uri.toASCIIString())); }
					catch (Exception e) {
						throw new TransformerException(e); }}
				return source;
			}
		});
	}
	
	static URIResolver getModuleUriResolver(Collection<String> classPath) {
		ClassLoader restoreClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			URLClassLoader classLoader; {
				URL[] classPathURLs = new URL[classPath.size()]; {
					int i = 0;
					for (String path : classPath)
						classPathURLs[i++] = new File(path).toURI().toURL();
				}
				classLoader = new URLClassLoader(classPathURLs, Thread.currentThread().getContextClassLoader());
			}
			Thread.currentThread().setContextClassLoader(classLoader);
			for (URIResolver r : ServiceLoader.load(URIResolver.class))
				if (r instanceof ModuleUriResolver)
					return r;
			throw new RuntimeException("No ModuleUriResolver found");
		} catch (MalformedURLException e) {
			throw new RuntimeException("No ModuleUriResolver found", e);
		} finally {
			Thread.currentThread().setContextClassLoader(restoreClassLoader);
		}
	}
}
