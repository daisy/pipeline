package org.daisy.dotify.common.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class URLCacheTest {
	
	@Test
	public void testFileURL() throws MalformedURLException, IOException {
		URLCache cache = new URLCache();
		cache.openStream(this.getClass().getResource("resource-files/file.txt"));
	}
	
	@Test
	public void testHttpURL() throws MalformedURLException, IOException {
		URLCache cache = new URLCache();
		cache.openStream(new URL("http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd"));
	}

}
