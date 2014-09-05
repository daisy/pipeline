package org.daisy.dotify.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import se.mtm.common.io.FileIO;
import se.mtm.common.xml.XMLInfo;
import se.mtm.common.xml.XMLTools;
import se.mtm.common.xml.XMLToolsException;

public class XMLToolsTest {
	
	@Test
	public void testXMLInfo_01() throws XMLToolsException, URISyntaxException, IOException {
		File f = null;
		try {
			f = getResourceCopy("resource-files/wellformed-01.xml");

			XMLInfo info = XMLTools.parseXML(f);
			assertEquals("http://www.example.com/ns/test", info.getUri());
			assertEquals("root", info.getLocalName());
		} finally {
			if (f!=null) {
				f.delete();
			}
		}

	}
	
	@Test
	public void testXMLInfo_02() throws XMLToolsException, URISyntaxException, IOException {
		File f = null;
		try {
			f = getResourceCopy("resource-files/wellformed-02.xml");
			XMLInfo info = XMLTools.parseXML(f);
			assertEquals("http://www.example.com/ns/test", info.getUri());
			assertEquals("root", info.getLocalName());
		} finally {
			if (f!=null) {
				f.delete();
			}
		}
	}

	private File getResourceCopy(String path) throws IOException {
		File f = File.createTempFile("junit", ".tmp");
		f.deleteOnExit();
		FileOutputStream os = new FileOutputStream(f);
		FileIO.copy(this.getClass().getResourceAsStream(path), os);
		return f;
	}

}
