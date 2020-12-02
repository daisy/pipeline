package org.daisy.common.file;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class URLsTest {

	@Test
	public void testRelativize() {
		assertEquals("x86_64/liblouis.dylib",
		             URLs.relativize(URLs.asURI("jar:file:/liblouis-native.jar!/native/macosx/"),
		                             URLs.asURI("jar:file:/liblouis-native.jar!/native/macosx/x86_64/liblouis.dylib"))
		                 .toASCIIString());
	}

	@Test
	public void testResolve() {
		assertEquals("jar:file:/foo.jar!/b.xml",
		             URLs.resolve(URLs.asURI("jar:file:/foo.jar!/a.xml"),
		                          URLs.asURI("b.xml"))
		                 .toASCIIString());
	}
	
	@Test
	public void testResolve2() {
		assertEquals("file:/b.xml",
		             URLs.resolve(URLs.asURI("jar:file:/tmp/foo.jar!/a.xml"),
		                          URLs.asURI("file:/b.xml"))
		                 .toASCIIString());
	}
}
