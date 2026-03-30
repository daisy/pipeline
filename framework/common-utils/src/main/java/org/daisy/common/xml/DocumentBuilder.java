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

	/**
	 * Parse an input using one of the provided parsers.
	 *
	 * @throws SAXException if the input could not be parsed
	 * @throws IOException if the input could not be read
	 */
	public static Document parse(InputSource input, String contentType, Iterable<DocumentBuilder> parsers)
			throws SAXException, IOException{
		boolean isXml = "".equals(contentType) || contentType.matches("[^ ]*(/|\\+)xml");
		DocumentBuilder xmlParser = null;
		for (DocumentBuilder p : parsers) {
			if (xmlParser == null && p.supportsContentType("text/xml")) {
				xmlParser = p;
				if (isXml)
					break;
			}
			if (!isXml && p.supportsContentType(contentType))
				return p.parse(input);
		}
		if (xmlParser != null)
			// content-type is either XML, not specified, or not recognized
			// in all cases we use (fallback to) the XML parser
			return xmlParser.parse(input);
		throw new SAXException("No parser found that supports the content type");
	}
}
