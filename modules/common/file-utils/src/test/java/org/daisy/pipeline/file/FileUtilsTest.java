package org.daisy.pipeline.file;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FileUtilsTest {

	@Test
	public void testExpand83() {
		File file = new MockFile("C:\\DOCUME~1\\file.xml");
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
			String uri = FileUtils.expand83(file).toString();
			Assert.assertEquals("URIs should be 8.3-decoded'",
			                    "file:///C:/Documents%20and%20Settings/file.xml",
			                    uri);
		} catch (Throwable e) {
			Assert.fail("An error occured while calling expand83: " + e.getMessage());
		}
		try {
			String uri = FileUtils.expand83("http://www.daisy.org/DOCUME~1/file.xml").toString();
			Assert.assertEquals("Only file: URIs should be 8.3-decoded'",
			                    "http://www.daisy.org/DOCUME~1/file.xml",
			                    uri);
		} catch (Throwable e) {
			Assert.fail("An error occured while calling expand83: " + e.getMessage());
		}
	}
}
