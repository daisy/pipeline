package org.daisy.common.file;

import org.junit.Assert;
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

	@Test
	public void testExpand83() {
		java.io.File file = new MockFile("C:\\DOCUME~1\\file.xml");
		try {
			String path = file.getCanonicalPath();
			Assert.assertEquals(
				"The mock File implementation should 8.3-decode the 'DOCUME~1' string (OS- and file system-independent)",
				"C:\\Documents and Settings\\file.xml",
				path);
		} catch (Throwable e) {
			Assert.fail("An error occured while calling getCanonicalPath: " + e.getMessage());
		}
		try {
			String uri = URLs.expand83(file).toString();
			Assert.assertEquals("URIs should be 8.3-decoded'",
			                    "file:///C:/Documents%20and%20Settings/file.xml",
			                    uri);
		} catch (Throwable e) {
			Assert.fail("An error occured while calling expand83: " + e.getMessage());
		}
		try {
			String uri = URLs.expand83("http://www.daisy.org/DOCUME~1/file.xml").toString();
			Assert.assertEquals("Only file: URIs should be 8.3-decoded'",
			                    "http://www.daisy.org/DOCUME~1/file.xml",
			                    uri);
		} catch (Throwable e) {
			Assert.fail("An error occured while calling expand83: " + e.getMessage());
		}
	}
}
