package org.daisy.pipeline.maven.plugin;

import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.daisy.common.spi.ServiceLoader;

import org.daisy.maven.xproc.calabash.Calabash;

import org.daisy.pipeline.modules.impl.ModuleUriResolver;

import static org.daisy.pipeline.maven.plugin.utils.URLs.asURI;

import org.xml.sax.InputSource;

class CalabashWithPipelineModules extends Calabash {
	
	CalabashWithPipelineModules(ClassLoader classPath) {
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
	
	// also used in HtmlizeSourcesMojo
	static URIResolver getModuleUriResolver(ClassLoader classLoader) {
		ClassLoader restoreClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(classLoader);
			for (URIResolver r : ServiceLoader.load(URIResolver.class))
				if (r instanceof ModuleUriResolver)
					return r;
			throw new RuntimeException("No ModuleUriResolver found");
		} finally {
			Thread.currentThread().setContextClassLoader(restoreClassLoader);
		}
	}
}
