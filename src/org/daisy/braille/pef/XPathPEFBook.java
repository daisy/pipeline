package org.daisy.braille.pef;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class XPathPEFBook {
	private static final Pattern eightDotPattern = Pattern.compile("[\u2840-\u28ff]");
	
	static PEFBook load(URI uri) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
		return load(uri, false);
	}

	static PEFBook load(URI uri, boolean continueOnError) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		HashMap<String, List<String>> metadata;
		// Book properties
		int volumes;
		int pageTags;
		int pages;
		int maxWidth;
		int maxHeight;
		String inputEncoding;
		boolean containsEightDot;
		int[] startPages;
		int tmp = 0;
		Document d = null;
		String encoding = null;
		metadata = new HashMap<String, List<String>>();
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			d = db.parse(uri.toString());
			encoding = d.getInputEncoding();
			List<String> al;
			NodeList nl = d.getDocumentElement().getElementsByTagName("meta").item(0).getChildNodes();
			for (int i=0; i<nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n!=null && n.getNodeType()==Node.ELEMENT_NODE) {
					if ("http://purl.org/dc/elements/1.1/".equals(n.getNamespaceURI())) { 
						String name = n.getLocalName();
						if (metadata.containsKey(name)) {
							al = metadata.remove(name);
						} else {
							al = new ArrayList<String>();
						}
						al.add(n.getTextContent());
						metadata.put(name, al);
					}
				}
			}
		} catch (ParserConfigurationException e) {
			if (continueOnError) {
				e.printStackTrace();
			} else {
				throw e;
			}
		} catch (SAXException e) {
			if (continueOnError) {
				e.printStackTrace();
			} else {
				throw e;
			}
		} catch (IOException e) {
			if (continueOnError) {
				e.printStackTrace();
			} else {
				throw e;
			}
		}
		
		inputEncoding = encoding;

		XPath xp = XPathFactory.newInstance().newXPath();
		xp.setNamespaceContext(new PEFNamespaceContext());

		// Count volumes
		tmp = 0;
		try {
			tmp = ((Double)xp.evaluate("count(//pef:volume)", d, XPathConstants.NUMBER)).intValue();
		} catch (XPathExpressionException e) {
			tmp = 0;
		}
		volumes = tmp;
		
		// Count page tags
		tmp = 0;
		try {
			tmp = ((Double)xp.evaluate("count(//pef:page)", d, XPathConstants.NUMBER)).intValue();
		} catch (XPathExpressionException e) {
			if (continueOnError) {
				tmp = 0;
			} else {
				throw e;
			}
		}
		pageTags = tmp;

		// Count pages including blank
		tmp = 0;
		try {
			tmp = ((Double)xp.evaluate(
				"count(//pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(//pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(//pef:section[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])-count(((//pef:section)[last()])[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])", d, XPathConstants.NUMBER)).intValue();
		} catch (XPathExpressionException e) {
			if (continueOnError) {
				tmp = 0;
			} else {
				throw e;
			}
		}
		pages = tmp;
		
		// Get max width
		tmp = 0;
		try {
			NodeList ns = (NodeList)xp.evaluate("//pef:*/@cols", d, XPathConstants.NODESET);
			for (int i = 0; i < ns.getLength(); ++i) {
				Attr attr = (Attr)ns.item(i);
				String colsValue = attr.getNodeValue();
				tmp = Math.max(tmp, Integer.valueOf(colsValue));				
			}
		} catch (XPathExpressionException e) {
			if (continueOnError) {
				tmp = 0;
			} else {
				throw e;
			}
		}
		maxWidth = tmp;
		
		// Get max height
		tmp = 0;
		try {
			NodeList ns = (NodeList)xp.evaluate("//pef:*/@rows", d, XPathConstants.NODESET);
			for (int i = 0; i < ns.getLength(); ++i) {
				Attr attr = (Attr)ns.item(i);
				String colsValue = attr.getNodeValue();
				tmp = Math.max(tmp, Integer.valueOf(colsValue));				
			}
		} catch (XPathExpressionException e) {
			if (continueOnError) {
				tmp = 0;
			} else {
				throw e;
			}
		}
		maxHeight = tmp;
		
		// Contains eight dot?
		boolean bTmp = false;
		try {
			NodeList texts = (NodeList)xp.evaluate("//pef:row/text()", d, XPathConstants.NODESET);
			for (int i = 0; i < texts.getLength(); ++i) {
				String text = texts.item(i).getTextContent();
				if (eightDotPattern.matcher(text).find()) {
					bTmp = true;
				}
			}
		} catch (XPathExpressionException e) {
			if (!continueOnError) {
				throw e;
			}
		}
		containsEightDot = bTmp;
		
		// get start pages
		startPages = new int[volumes];
		for (int i = 1; i <= volumes; i++) {
			try {
				Node page = (Node)xp.evaluate("(//pef:volume)[position()="+(i)+"]/descendant::pef:page[1]", d, XPathConstants.NODE);
				int pageOffset = ((Double)xp.evaluate("count(preceding::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 + count(preceding::pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + count(preceding::pef:section[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])", page, XPathConstants.NUMBER)).intValue();
				startPages[i-1] = pageOffset + 1;
			} catch (XPathExpressionException e) { 
				if (continueOnError) {
					e.printStackTrace();
					startPages[i-1] = 0;
				} else {
					throw e;
				}
			}
		}
		return new PEFBook(metadata, volumes, pages, pageTags, maxWidth, maxHeight, inputEncoding, containsEightDot, startPages);
	}
}
