package org.daisy.dotify.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

import se.mtm.common.xml.XMLInfo;
import se.mtm.common.xml.XMLTools;
import se.mtm.common.xml.XMLToolsException;

public class XMLToolsTest {
	
	@Test
	public void testXMLInfo_01() throws XMLToolsException, URISyntaxException {
		XMLInfo info = XMLTools.parseXML(new File(this.getClass().getResource("resource-files/wellformed-01.xml").toURI()));
		assertEquals("http://www.example.com/ns/test", info.getUri());
		assertEquals("root", info.getLocalName());
	}
	
	@Test
	public void testXMLInfo_02() throws XMLToolsException, URISyntaxException {
		XMLInfo info = XMLTools.parseXML(new File(this.getClass().getResource("resource-files/wellformed-02.xml").toURI()));
		assertEquals("http://www.example.com/ns/test", info.getUri());
		assertEquals("root", info.getLocalName());
	}

}
