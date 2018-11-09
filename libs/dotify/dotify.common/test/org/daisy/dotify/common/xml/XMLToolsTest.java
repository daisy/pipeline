package org.daisy.dotify.common.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;

import org.daisy.dotify.common.io.FileIO;
import org.junit.Test;

@SuppressWarnings("javadoc")
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
	
	@Test
	public void testXmlDeclarationPattern_01() {
		String input = "  <?xml version = \"1.0\" encoding = 'utf-8' standalone='true'?> <body>";
		Matcher m = XMLTools.XML_DECL.matcher(input);
		assertTrue(m.find());
		assertEquals("utf-8", m.group("ENCODING"));
		assertFalse(m.find());
	}
	
	@Test
	public void testXmlDeclarationPattern_02() {
		String input = "  <body>";
		Matcher m = XMLTools.XML_DECL.matcher(input);
		assertFalse(m.find());
	}

	@Test
	public void testXmlDeclarationPattern_03() {
		String input = "\uFEFF<?xml version='1.0' encoding='utf-16'?>\n<dtbook xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\" xml:lang=\"sv\" version=\"2005-3\">\n</dtbook>";
		Matcher m = XMLTools.XML_DECL.matcher(input);
		assertTrue(m.find());
		assertEquals("utf-16", m.group("ENCODING"));
		assertFalse(m.find());
	}
	
	@Test
	public void testXmlDeclaration_01() {
		String input = "  <?xml version = \"1.0\" encoding = 'utf-8' standalone='true'?> <body>";
		assertEquals("utf-8", XMLTools.getDeclaredEncoding(input).get());
	}
	
	@Test
	public void testXmlDeclaration_02() {
		String input = " <body>";
		assertEquals(Optional.empty(), XMLTools.getDeclaredEncoding(input));
	}
	
	@Test
	public void testXMLEncoding_01() throws XmlEncodingDetectionException {
		byte[] data = encode(StandardCharsets.UTF_8, "utf-8", false);
		assertEquals("utf-8", XMLTools.detectXmlEncoding(data));
	}
	
	@Test
	public void testXMLEncoding_02() throws XmlEncodingDetectionException {
		byte[] data = encode(StandardCharsets.UTF_8, "utf-8", true);
		assertEquals("UTF-8", XMLTools.detectXmlEncoding(data));
	}

	@Test
	public void testXMLEncoding_03() throws XmlEncodingDetectionException {
		// This is what the Oxygen XML editor produced when specifying utf-16 in the declaration
		byte[] data = encode(StandardCharsets.UTF_16LE, "utf-16", true);
		assertEquals("UTF-16LE", XMLTools.detectXmlEncoding(data));
	}

	@Test
	public void testXMLEncoding_04() throws XmlEncodingDetectionException {
		byte[] data = encode(StandardCharsets.UTF_16BE, "utf-16be", false);
		assertEquals("utf-16be", XMLTools.detectXmlEncoding(data));
	}

	@Test
	public void testXMLEncoding_05() throws XmlEncodingDetectionException {
		byte[] utf16le = encode(StandardCharsets.UTF_16LE, "utf-16le", false);
		assertEquals("utf-16le", XMLTools.detectXmlEncoding(utf16le));
	}

	@Test
	public void testXMLEncoding_06() throws XmlEncodingDetectionException {
		byte[] data = encode(Charset.forName("ibm500"), "ibm500", false);
		assertEquals("ibm500", XMLTools.detectXmlEncoding(data));
	}

	@Test
	public void testBomEncoding_01() {
		assertEquals(Optional.empty(), XMLTools.detectBomEncoding(new byte[]{}));
	}

	@Test
	public void testBomEncoding_02() {
		assertEquals(Optional.empty(), XMLTools.detectBomEncoding(new byte[]{'a'}));
		//byte[] data = "abcd".getBytes("utf-8")
	}

	@Test
	public void testBomEncoding_03() {
		assertEquals(Optional.empty(), XMLTools.detectBomEncoding(new byte[]{'a', 'b'}));
	}

	@Test
	public void testBomEncoding_04() throws UnsupportedEncodingException {
		assertEquals(Optional.of(StandardCharsets.UTF_8), XMLTools.detectBomEncoding("\uFEFF".getBytes("utf-8")));
	}
	
	@Test
	public void testBomEncoding_05() throws UnsupportedEncodingException {
		assertEquals(Optional.empty(), XMLTools.detectBomEncoding("abc".getBytes("utf-8")));
	}
	
	@Test
	public void testBomEncoding_06() throws UnsupportedEncodingException {
		assertEquals(Optional.of(StandardCharsets.UTF_16LE), XMLTools.detectBomEncoding("\uFEFF".getBytes("utf-16LE")));
	}

	private byte[] encode(Charset charset, String name, boolean bom) {
		String data = (bom?"\uFEFF":"")+"<?xml version='1.0' encoding='"+name+"'?>\n" + 
				"<dtbook xmlns=\"http://www.daisy.org/z3986/2005/dtbook/\" xml:lang=\"sv\" version=\"2005-3\">\n" + 
				"</dtbook>";
		return data.getBytes(charset);
	}

	private File getResourceCopy(String path) throws IOException {
		File f = File.createTempFile("junit", ".tmp");
		f.deleteOnExit();
		FileOutputStream os = new FileOutputStream(f);
		FileIO.copy(this.getClass().getResourceAsStream(path), os);
		return f;
	}

}
