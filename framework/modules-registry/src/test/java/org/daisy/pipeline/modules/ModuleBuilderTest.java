package org.daisy.pipeline.modules;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
		URI catalogUri = new File(resourcesDir, "catalog.xml").toURI();
		XmlCatalog catalog = catalogParser.parse(catalogUri);
		ResourceLoader resourceLoader = new ResourceLoader() {
			public URL loadResource(String path) {
				try {
					return new File(resourcesDir, path).toURI().toURL(); }
				catch (MalformedURLException e) {
					throw new RuntimeException(e); }
			}
			public Iterable<URL> loadResources(String path) {
				throw new UnsupportedOperationException("not implemented");
			}
		};
		Module module = new MyModuleBuilder().withLoader(resourceLoader).withCatalog(catalog).build();
		Iterator<Component> components = module.getComponents().iterator();
		assertTrue(components.hasNext());
		Component c = components.next();
		assertEquals("http://example-module/hello.xml", c.getURI().toString());
		assertFalse(components.hasNext());
	}
}
