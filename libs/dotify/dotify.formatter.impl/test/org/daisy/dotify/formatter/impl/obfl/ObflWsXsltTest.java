package org.daisy.dotify.formatter.impl.obfl;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.daisy.dotify.formatter.impl.obfl.OBFLWsNormalizer;
import org.junit.Ignore;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class ObflWsXsltTest {

	@Test
	public void testWsNormalizer_01() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-01.xml", "resource-files/ws-test-expected-01.xml", "01", false);
		assertTrue("Compare (Toc) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_02() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-02.xml", "resource-files/ws-test-expected-02.xml", "02", false);
		assertTrue("Compare (Block) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_03() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-03.xml", "resource-files/ws-test-expected-03.xml", "03", false);
		assertTrue("Compare (Span) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_04() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-04.xml", "resource-files/ws-test-expected-04.xml", "04", false);
		assertTrue("Compare (Line breaks) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_05() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-05.xml", "resource-files/ws-test-expected-05.xml", "05", false);
		assertTrue("Compare (Leader) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_06() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-06.xml", "resource-files/ws-test-expected-06.xml", "06", false);
		assertTrue("Compare (Evaluate) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_07() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-07.xml", "resource-files/ws-test-expected-07.xml", "07", false);
		assertTrue("Compare (Page number) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_08() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-08.xml", "resource-files/ws-test-expected-08.xml", "08", false);
		assertTrue("Compare (Marker) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_09() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-09.xml", "resource-files/ws-test-expected-09.xml", "09", false);
		assertTrue("Compare (Anchor) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_10() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-10.xml", "resource-files/ws-test-expected-10.xml", "10", false);
		assertTrue("Compare (Style) failed at byte: " + ret, ret == -1);
	}

	@Test
	public void testWsNormalizer_11() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-11.xml", "resource-files/ws-test-expected-11.xml", "11", false);
		assertTrue("Compare (NBSP) failed at byte: " + ret, ret == -1);
	}
	
	@Test
	public void testWsNormalizer_12() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-12.xml", "resource-files/ws-test-expected-12.xml", "12", false);
		assertTrue("Compare (Before/After) failed at byte: " + ret, ret == -1);
	}
	
	@Test
	public void testWsNormalizer_13() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-13.xml", "resource-files/ws-test-expected-13.xml", "13", false);
		assertTrue("Compare (Before/After) failed at byte: " + ret, ret == -1);
	}
	
	@Test
	public void testWsNormalizer_14() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-14.xml", "resource-files/ws-test-expected-14.xml", "14", false);
		assertTrue("Compare (Item) failed at byte: " + ret, ret == -1);
	}
	
	@Ignore // see https://github.com/joeha480/dotify/issues/117
	@Test
	public void testWsNormalizer_15() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-15.xml", "resource-files/ws-test-expected-15.xml", "15", false);
		assertTrue("Compare (Item) failed at byte: " + ret, ret == -1);
	}
	
	@Ignore
	@Test
	public void testWsNormalizer_16() throws IOException, XMLStreamException {
		int ret = testWsNormalizer("resource-files/ws-test-input-16.xml", "resource-files/ws-test-expected-16.xml", "16", false);
		assertTrue("Compare (Item) failed at byte: " + ret, ret == -1);
	}
	
	// Helpers
	public int testWsNormalizer(String input, String expected, String id, boolean keep) throws IOException, XMLStreamException {
		
		File in = File.createTempFile("TestInput-"+id+"-", ".tmp");
		copy(this.getClass().getResourceAsStream(input), new FileOutputStream(in));
		
		File normalizedFile = File.createTempFile("TestResult-"+id+"-", ".tmp");
		XMLInputFactory inFactory = XMLInputFactory.newInstance();
		inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
		inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		FileInputStream instream = new FileInputStream(in);
		FileOutputStream outstream = new FileOutputStream(normalizedFile);
		OBFLWsNormalizer t = new OBFLWsNormalizer(inFactory.createXMLEventReader(instream), XMLEventFactory.newInstance());
		t.parse(XMLOutputFactory.newInstance(), outstream);
		int ret = compareBinary(new FileInputStream(normalizedFile), this.getClass().getResourceAsStream(expected));
		
		try {
			instream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!keep && !normalizedFile.delete()) {
			normalizedFile.deleteOnExit();
		}
		if (!keep && !in.delete()) {
			in.deleteOnExit();
		}
		
		return ret;
	}

	public static void copy(InputStream is, OutputStream os) throws IOException {
		InputStream bis = new BufferedInputStream(is);
		OutputStream bos = new BufferedOutputStream(os);
		int b;
		while ((b = bis.read())!=-1) {
			bos.write(b);
		}
		bos.flush();
		bos.close();
		bis.close();
	}
	
	public int compareBinary(InputStream f1, InputStream f2) throws IOException {
		InputStream bf1 = new BufferedInputStream(f1);
		InputStream bf2 = new BufferedInputStream(f2);
		int pos = 0;
		try {
			int b1;
			int b2;
			while ((b1 = bf1.read())!=-1 & b1 == (b2 = bf2.read())) {
				pos++;
				//continue
			}
			if (b1!=-1 || b2!=-1) {
				return pos;
			}
			return -1;
		} finally {
			bf1.close();
			bf2.close();
		}
	}

}
