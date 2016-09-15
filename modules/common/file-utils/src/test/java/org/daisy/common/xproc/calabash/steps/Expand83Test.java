package org.daisy.common.xproc.calabash.steps;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import net.sf.saxon.trans.XPathException;

import org.daisy.saxon.functions.file.Expand83;
import org.junit.Test;

public class Expand83Test {
	
	@Test
	public void test() {
		File file = new MockFile("C:\\DOCUME~1\\file.xml");
		
		try {
			String path = file.getCanonicalPath();
			assertEquals("The mock File implementation should 8.3-decode the 'DOCUME~1' string (OS- and file system-independent)", "C:\\Documents and Settings\\file.xml", path);
		} catch (IOException e) {
			fail("An error occured while calling getCanonicalPath: "+e.getMessage());
		}
		
		try {
			String uri = Expand83.expand83(file);
			assertEquals("URIs should be 8.3-decoded'", "file:///C:/Documents%20and%20Settings/file.xml", uri);
		} catch (XPathException e) {
			fail("An error occured while calling expand83: "+e.getMessage());
		}
		
		try {
			String uri = Expand83.expand83("http://www.daisy.org/DOCUME~1/file.xml");
			assertEquals("Only file: URIs should be 8.3-decoded'", "http://www.daisy.org/DOCUME~1/file.xml", uri);
		} catch (XPathException e) {
			fail("An error occured while calling expand83: "+e.getMessage());
		}
	}

}
