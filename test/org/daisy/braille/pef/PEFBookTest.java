package org.daisy.braille.pef;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class PEFBookTest {

	@Test
	public void testSerializable() throws IOException, XPathExpressionException, ParserConfigurationException, SAXException, URISyntaxException, ClassNotFoundException {
		File f = File.createTempFile("PEFBookTest", ".tmp");
		try {
			PEFBook p1 = PEFBook.load(this.getClass().getResource("resource-files/PEFBookTestInput.pef").toURI());
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(p1);
			oos.close();
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			PEFBook p2 = (PEFBook)ois.readObject();
			ois.close();
			assertTrue("Assert that deserialized object is equal to original object", p1.equals(p2));
		} finally {
			if (!f.delete()) {
				f.deleteOnExit();
			}
		}
	}
}
