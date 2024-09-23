package org.daisy.common.xml;

import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Produces a document object model from a possibly non-XML file.
 */
public interface DocumentBuilder {

	/**
	 * Test whether this parser supports a given content media type.
	 *
	 * @param type a MIME type, e.g. "text/html"
	 */
	public boolean supportsContentType(String type);

	/**
	 * Parse the input
	 *
	 * @throws SAXException if the input could not be parsed
	 * @throws IOException if the input could not be read
	 */
	public Document parse(InputSource input) throws SAXException, IOException;

}
