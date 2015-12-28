package org.daisy.dotify.impl.input.epub;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class AbstractContainerReader {

	Document readFromStreamAsXML(File f) throws EPUB3ReaderException {
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return db.parse(f);
		} catch (ParserConfigurationException e) {
			throw new EPUB3ReaderException("Failed to read container.", e);
		} catch (SAXException e) {
			throw new EPUB3ReaderException("Failed to read container.", e);
		} catch (IOException e) {
			throw new EPUB3ReaderException("Failed to read container.", e);
		}
	}
}
