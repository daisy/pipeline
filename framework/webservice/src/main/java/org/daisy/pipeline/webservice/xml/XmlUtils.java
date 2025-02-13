package org.daisy.pipeline.webservice.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import org.xml.sax.SAXException;

public class XmlUtils {

	public static final String NS_DAISY = "http://www.daisy.org/ns/pipeline/data";
	public static final String NS_PIPELINE_DATA = "http://www.daisy.org/ns/pipeline/data";

	private static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);
	
	public static String nodeToString(Node node) {
		Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS)doc.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", false);
		String string = serializer.writeToString(node);
		return string.trim();
	}

	public static Document createDom(String documentElementName) {
		try {
			DocumentBuilderFactory documentBuilderFactory
				= DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder
				= documentBuilderFactory.newDocumentBuilder();
			DOMImplementation domImpl = documentBuilder.getDOMImplementation();
			Document document = domImpl.createDocument(
				NS_PIPELINE_DATA, documentElementName, null);
			return document;
		} catch (ParserConfigurationException e) {
			logger.warn("creating dom document",e);
			return null;
		}
	}

	public static Element parseXml(String xml, Document ownerDoc) throws IOException, SAXException {
		try {
			DocumentBuilderFactory documentBuilderFactory
				= DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder
				= documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(
				new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
			return (Element)ownerDoc.adoptNode(document.getDocumentElement());
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e); // should not happen
		}
	}
}
