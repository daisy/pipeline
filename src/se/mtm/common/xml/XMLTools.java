package se.mtm.common.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLTools {

	public static void transform(Object source, Object result, Object xslt, Map<String, Object> params) throws XMLToolsException {
		transform(toSource(source), toResult(result), toSource(xslt), params);
	}

	public static void transform(Source source, Result result, Source xslt, Map<String, Object> params) throws XMLToolsException {
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer(xslt);
		} catch (TransformerConfigurationException e) {
			throw new XMLToolsException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new XMLToolsException(e);
		}

		for (String name : params.keySet()) {
			transformer.setParameter(name, params.get(name));
		}

		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new XMLToolsException(e);
		}
	}

	private static Source toSource(Object source) throws XMLToolsException {
		if (source instanceof File) {
			return new StreamSource((File) source);
		} else if (source instanceof String) {
			return new StreamSource((String) source);
		} else if (source instanceof URL) {
			try {
				return new StreamSource(((URL) source).toURI().toString());
			} catch (URISyntaxException e) {
				throw new XMLToolsException(e);
			}
		} else if (source instanceof Source) {
			return (Source) source;
		} else {
			throw new XMLToolsException("Failed to create source: " + source);
		}
	}

	private static Result toResult(Object result) throws XMLToolsException {
		if (result instanceof File) {
			return new StreamResult((File) result);
		} else if (result instanceof OutputStream) {
			return new StreamResult((OutputStream) result);
		} else if (result instanceof String) {
			return new StreamResult((String) result);
		} else if (result instanceof URL) {
			try {
				return new StreamResult(((URL) result).toURI().toString());
			} catch (URISyntaxException e) {
				throw new XMLToolsException(e);
			}
		} else if (result instanceof Result) {
			return (Result) result;
		} else {
			throw new XMLToolsException("Failed to create result: " + result);
		}
	}

	/**
	 * Returns true if the specified file is well formed XML.
	 * @param f the file
	 * @return returns true if the file is well formed XML, false otherwise
	 * @throws XMLToolsException
	 */
	public final static boolean isWellformedXML(File f) throws XMLToolsException {
		return parseXML(f)!=null;
	}
	
	/**
	 * Asserts that the specified file is well formed and returns some root node information.
	 * @param f the file
	 * @return returns the root node, or null if file is not well formed
	 * @throws XMLToolsException
	 */
	public final static XMLInfo parseXML(File f) throws XMLToolsException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser saxParser = null;
		try {
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new XMLToolsException("Failed to set up XML parser.", e);
		} catch (SAXException e) {
			throw new XMLToolsException("Failed to set up XML parser.", e);
		}
		XMLHandler dh = new XMLHandler();
		try {
			saxParser.parse(f, dh);
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			throw new XMLToolsException(e);
		}
		return dh.root;
	}
	
	private static class XMLHandler extends DefaultHandler {
		private XMLInfo root = null;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (this.root == null) {
				this.root = new XMLInfo(uri, localName, qName, attributes);
			}
		}
	}

}
