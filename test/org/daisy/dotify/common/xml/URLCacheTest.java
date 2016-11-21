package org.daisy.dotify.common.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.daisy.dotify.common.xml.URLCache;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class URLCacheTest {
	
	@Test
	public void testFileURL() throws MalformedURLException, IOException {
		URLCache cache = new URLCache();
		InputStream is = cache.openStream(this.getClass().getResource("resource-files/file.txt"));
		assertNotNull(is);
	}
	
	@Test
	public void testHttpURL() throws MalformedURLException, IOException {
		URLCache cache = new URLCache();
		InputStream is = cache.openStream(new URL("http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd"));
		assertNotNull(is);
	}
	
	@Test
	public void testMissingPart() throws MalformedURLException, IOException {
		URLCache cache = new URLCache();
		assertNull(cache.getCacheFile(new URL("http://www.example.com")));
	}

}
