package org.daisy.pipeline.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;

import org.daisy.pipeline.xmlcatalog.XmlCatalog;
import org.daisy.pipeline.xmlcatalog.impl.StaxXmlCatalogParser;
import org.junit.Test;

public class JarModuleBuilderTest {

	@Test
	public void testFileBuildModule() {
		StaxXmlCatalogParser catalogParser = new StaxXmlCatalogParser();
		catalogParser.setFactory(XMLInputFactory.newInstance());
		catalogParser.activate();
		final File resourcesDir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
		URI catalogUri = new File(resourcesDir, "META-INF/catalog.xml").toURI();
		XmlCatalog catalog = catalogParser.parse(catalogUri);
		Module module = new JarModuleBuilder()
				.withJarFile(resourcesDir)
				.withCatalog(catalog)
				.build();
		Iterator<Component> components = module.getComponents().iterator();
		assertTrue(components.hasNext());
		Component c = components.next();
		assertEquals("http://example-module/rewrite-uri-ok/test.xml", c.getURI().toString());
		assertTrue(c.getResource().toString().endsWith("test-rewrite-uri/test.xml"));
		assertTrue(new File(c.getResource()).exists());
		c = components.next();
		assertEquals("http://example-module/hello.xml", c.getURI().toString());
		assertTrue(new File(c.getResource()).exists());
		c = components.next();
		assertEquals("http://example-module/rewrite-sub-ok/test.xml", c.getURI().toString());
		assertTrue(c.getResource().toString().endsWith("test-rewrite-sub/test2/test.xml"));
		assertFalse(components.hasNext());
		
	}
	@Test
	public void testJarBuildModule() {
		StaxXmlCatalogParser catalogParser = new StaxXmlCatalogParser();
		catalogParser.setFactory(XMLInputFactory.newInstance());
		catalogParser.activate();
		
		final File resourcesJar = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()+"test.jar");
		
		Module module = new JarModuleBuilder()
				.withJarFile(resourcesJar)
				.withCatalogParser(catalogParser)
				.build();
		Iterator<Component> components = module.getComponents().iterator();
		assertTrue(components.hasNext());
		Component c;
		c = components.next();
		assertEquals("http://example-module/rewrite-uri-ok/test.xml", c.getURI().toString());
		assertTrue(c.getResource().toString().endsWith("test-rewrite-uri/test.xml"));
		assertTrue(components.hasNext());
		c = components.next();
		assertEquals("http://example-module/hello.xml", c.getURI().toString());
		c = components.next();
		assertEquals("http://example-module/rewrite-sub-ok/test.xml", c.getURI().toString());
		assertTrue(c.getResource().toString().endsWith("test-rewrite-sub/test2/test.xml"));
		assertFalse(components.hasNext());
		
	}
	
	
}
