package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import com.ctc.wstx.stax.WstxInputFactory;

import org.daisy.maven.xproc.calabash.Calabash;

import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.impl.resolver.ModuleUriResolver;
import org.daisy.pipeline.modules.impl.tracker.OSGIModuleBuilder;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.daisy.pipeline.modules.ResourceLoader;
import org.daisy.pipeline.xmlcatalog.impl.StaxXmlCatalogParser;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;

import static org.daisy.pipeline.maven.plugin.utils.URIs.asURI;
import static org.daisy.pipeline.maven.plugin.utils.URLs.resolve;

import org.xml.sax.InputSource;

class CalabashWithPipelineModules extends Calabash {
	
	CalabashWithPipelineModules(Iterable<String> classPath) {
		super();
		final ModuleUriResolver resolver = new ModuleUriResolver();
		resolver.setModuleRegistry(new OSGilessModuleRegistry(classPath));
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
	
	/*
	 * Alternative ModuleRegistry that does not make use of OSGi's
	 * BundleTracker, but instead just looks for all /META-INF/catalog.xml
	 * files on the class path.
	 *
	 * TODO: can be deleted when merged with "osgi-less" branch
	 */
	static class OSGilessModuleRegistry implements ModuleRegistry {
		
		private final HashMap<URI,Module> components = new HashMap<URI,Module>();
		private final HashMap<String,Module> entities = new HashMap<String,Module>();
		
		public OSGilessModuleRegistry(Iterable<String> classPath) {
			StaxXmlCatalogParser catalogParser = new StaxXmlCatalogParser();
			catalogParser.setFactory(new WstxInputFactory());
			catalogParser.activate();
			for (String p : classPath) {
				File f = new File(p);
				final URI uri = f.isDirectory() ?
					asURI(new File(f, "META-INF/catalog.xml")) :
					asURI("jar:" + asURI(f).toASCIIString() + "!/META-INF/catalog.xml");
				try {
					uri.toURL().openConnection().connect();
				} catch (IOException e) {
					continue;
				}
				XmlCatalog catalog = catalogParser.parse(uri);
				ResourceLoader resourceLoader = new ResourceLoader() {
					public URL loadResource(String path) {
						return resolve(uri, path);
					}
					public Iterable<URL> loadResources(String path) {
						throw new UnsupportedOperationException("not implemented");
					}
				};
				// using OSGIModuleBuilder but not calling withBundle()
				Module module = new OSGIModuleBuilder().withLoader(resourceLoader).withCatalog(catalog).build();
				for (Component component : module.getComponents())
					components.put(component.getURI(), module);
				for (Entity entity: module.getEntities())
					entities.put(entity.getPublicId(), module);
			}
		}
		
		public Module getModuleByComponent(URI uri) {
			return components.get(uri);
		}
		
		public Module getModuleByEntity(String publicId) {
			return entities.get(publicId);
		}
		
		public Iterator<Module> iterator() {
			throw new UnsupportedOperationException("not implemented"); }
		public void addModule(Module module) {
			throw new UnsupportedOperationException("not implemented"); }
		public Module resolveDependency(URI component, Module source) {
			throw new UnsupportedOperationException("not implemented"); }
		public Iterable<URI> getComponents() {
			throw new UnsupportedOperationException("not implemented"); }
		public Iterable<String> getEntities() {
			throw new UnsupportedOperationException("not implemented"); }
	}
}
