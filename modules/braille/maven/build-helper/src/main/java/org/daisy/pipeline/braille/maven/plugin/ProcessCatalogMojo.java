package org.daisy.pipeline.braille.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import com.ctc.wstx.stax.WstxInputFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.calabash.Calabash;

import org.daisy.pipeline.braille.maven.plugin.utils.URLs;
import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.impl.resolver.ModuleUriResolver;
import org.daisy.pipeline.modules.impl.tracker.OSGIModuleBuilder;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.daisy.pipeline.modules.ResourceLoader;
import org.daisy.pipeline.xmlcatalog.impl.StaxXmlCatalogParser;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;

import org.xml.sax.InputSource;

@Mojo(
	name = "process-catalog",
	defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
	requiresDependencyResolution = ResolutionScope.COMPILE
)
public class ProcessCatalogMojo extends AbstractMojo {
	
	@Parameter(
		readonly = true,
		defaultValue = "${project.basedir}/src/main/resources/META-INF/catalog.xml"
	)
	private File catalogFile;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project.build.directory}/generated-resources/"
	)
	private File outputDirectory;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project.version}"
	)
	private String projectVersion;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project}"
	)
	private MavenProject mavenProject;
	
	public void execute() throws MojoFailureException {
		try {
			final ModuleUriResolver resolver = new ModuleUriResolver();
			resolver.setModuleRegistry(new OSGilessModuleRegistry(mavenProject.getCompileClasspathElements()));
			XProcEngine engine = new Calabash() {
				{
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
			};
			engine.run(asURI(this.getClass().getResource("/org/daisy/pipeline/braille/build/process-catalog.xpl")).toASCIIString(),
			           ImmutableMap.of("source", (List<String>)ImmutableList.of(asURI(catalogFile).toASCIIString())),
			           null,
			           ImmutableMap.of("outputDir", asURI(outputDirectory).toASCIIString(),
			                           "version", projectVersion),
			           null);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
	
	/*
	 * Alternative ModuleRegistry that does not make use of OSGi's
	 * BundleTracker, but instead just looks for all /META-INF/catalog.xml
	 * files on the class path.
	 *
	 * TODO: move to pipeline-framework.
	 *
	 * TODO (2): an even better alternative would be to provide the "Module"
	 * services (or even individual "Component" services) by the modules
	 * themselves. These could be OSGi and/or SPI services. This would give
	 * more control to the modules themselves about which resources they
	 * provide, under which circumstances, and how.
	 */
	private static class OSGilessModuleRegistry implements ModuleRegistry {
		
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
						return URLs.resolve(uri, path);
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
	
	// TODO: use org.daisy.pipeline.braille.common.util.URIs.asURI
	private static URI asURI(Object o) {
		if (o == null)
			return null;
		try {
			if (o instanceof String)
				return new URI((String)o);
			if (o instanceof File)
				return ((File)o).toURI();
			if (o instanceof URL) {
				URL url = (URL)o;
				if (url.getProtocol().equals("jar"))
					return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
				String authority = (url.getPort() != -1) ?
					url.getHost() + ":" + url.getPort() :
					url.getHost();
				return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef()); }
			if (o instanceof URI)
				return (URI)o; }
		catch (Exception e) {}
		throw new RuntimeException("Object can not be converted to URI: " + o);
	}
}
