package org.daisy.dotify.tasks.impl.input.text;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class Text2ObflWriterTest {

	public Text2ObflWriterTest() {
	}

	@Test
	public void testTask() throws IOException, TransformerException {
		InputStream is = this.getClass().getResourceAsStream("resource-files/input.txt");
		File f = File.createTempFile("junit-", ".tmp");
		f.deleteOnExit();
		// File f = new File("C:\\out.obfl");
		OutputStream os = new FileOutputStream(f);
		Text2ObflWriter fw = new Text2ObflWriter(is, os, "utf-8");
		fw.setRootLang("en");
		fw.parse();
		FileCompare fc = new FileCompare();
		assertTrue("File not equal", fc.compareXML(new FileInputStream(f), this.getClass().getResourceAsStream("resource-files/expected.obfl")));
	}
	
	@Test
	public void testTaskBOM() throws IOException, TransformerException {
		InputStream is = this.getClass().getResourceAsStream("resource-files/input-bom.txt");
		File f = File.createTempFile("junit-", ".tmp");
		f.deleteOnExit();
		// File f = new File("C:\\out.obfl");
		OutputStream os = new FileOutputStream(f);
		Text2ObflWriter fw = new Text2ObflWriter(is, os, "utf-8");
		fw.setRootLang("en");
		fw.parse();
		FileCompare fc = new FileCompare();
		assertTrue("File not equal", fc.compareXML(new FileInputStream(f), this.getClass().getResourceAsStream("resource-files/expected.obfl")));
	}

}
