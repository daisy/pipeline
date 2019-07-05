package org.daisy.pipeline.modules;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;

import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ResourceLoader;
import org.daisy.pipeline.xmlcatalog.impl.StaxXmlCatalogParser;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ModuleBuilderTest {
	
	class MyModuleBuilder extends AbstractModuleBuilder<MyModuleBuilder> {
		public MyModuleBuilder self() {
			return this;
		}
	}
	
	@Test
	public void testBuildModule() {
		StaxXmlCatalogParser catalogParser = new StaxXmlCatalogParser();
		catalogParser.setFactory(XMLInputFactory.newInstance());
		catalogParser.activate();
		final File resourcesDir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
		URI catalogUri = new File(resourcesDir, "META-INF/catalog.xml").toURI();
		XmlCatalog catalog = catalogParser.parse(catalogUri);
		// mimicking the osgi resource loader
		ResourceLoader resourceLoader = new ResourceLoader() {
			public URL loadResource(String path) {
				try {
					if (!path.startsWith("../")) {
						throw new RuntimeException("Paths must start with '../' but got '" + path + "'");
					}
					path = path.substring(3);
					File result = new File(resourcesDir, path);
					if(!result.exists()) 
						throw new MalformedURLException(result.toURI().toURL() + " does not exsist");
					return result.toURI().toURL();
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
			public Iterable<URL> loadResources(String path) {
				ArrayList<URL> result = new ArrayList<URL>();
				if(path.startsWith("../")) path = path.substring(3); // Delete ../
				File currentElement = new File(resourcesDir, path);
				if(!currentElement.exists()) {
					throw new RuntimeException(path + " was not found in the resource directory of the module when loading the resources");
				} else if(currentElement.isFile()) {
					try {
						result.add(currentElement.toURI().toURL()); }
					catch (MalformedURLException e) {
						throw new RuntimeException(e); }
				} else {
					for (String fileName : currentElement.list()) {
						ArrayList<URL> temp = (ArrayList<URL>) this.loadResources(
								path + 
								(!path.endsWith("/") ? "/" : "") + 
								fileName);
						result.addAll(temp);
					}
				}
				return result;
			}
		};
		Module module = new MyModuleBuilder()
				.withLoader(resourceLoader)
				.withCatalog(catalog)
				.build();
		Iterator<Component> components = module.getComponents().iterator();
		assertTrue(components.hasNext());
		Component c = components.next();
		assertEquals("http://example-module/rewrite-uri-ok/test.xml", c.getURI().toString());
		assertTrue(c.getResource().toString().endsWith("test-rewrite-uri/test.xml"));
		c = components.next();
		assertEquals("http://example-module/hello.xml", c.getURI().toString());
		assertFalse(components.hasNext());
	}
}
