package org.daisy.braille.pef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * Provides a loader for PEFBooks that keeps serialized PEFBooks that can be loaded 
 * quickly when the source is unchanged.
 *  
 * @author Joel Håkansson
 *
 */
public class PEFBookLoader {
	private final File dir;
	
	public PEFBookLoader() {
		 this(new File(System.getProperty("java.io.tmpdir")));
	}
	
	public PEFBookLoader(File dir) {
		System.out.println(dir);
		this.dir = dir;
	}
	
	public PEFBook load(File f) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		File serial = new File(dir, f.getName()+"-"+f.hashCode()+".v2meta");
		PEFBook book;
		if (serial.exists() && serial.lastModified()>f.lastModified()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serial));
			try {
				book = (PEFBook)ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				book = PEFBook.load(f.toURI());
				if (!serial.delete()) {
					serial.deleteOnExit();
				}
			} finally {
				ois.close();
			}
		} else {
			book = PEFBook.load(f.toURI());
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serial));
			try {
				oos.writeObject(book);
			} finally {
				oos.close();
			}
		}
		return book;
	}
}
