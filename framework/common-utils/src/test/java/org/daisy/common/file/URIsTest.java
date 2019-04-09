package org.daisy.common.file;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class URIsTest {

	@Test
	public void testRelativize() {
		assertEquals("x86_64/liblouis.dylib",
		             URIs.relativize("jar:file:/liblouis-native.jar!/native/macosx/",
		                             "jar:file:/liblouis-native.jar!/native/macosx/x86_64/liblouis.dylib")
		                 .toString());
	}

	@Test
	public void testResolve() {
		assertEquals("jar:file:/foo.jar!/b.xml",
		             URIs.resolve("jar:file:/foo.jar!/a.xml",
		                          "b.xml")
		                 .toString());
	}
	
	@Test
	public void testResolve2() {
		assertEquals("file:/b.xml",
		             URIs.resolve("jar:file:/tmp/foo.jar!/a.xml",
		                          "file:/b.xml")
		                 .toString());
	}
}
