package org.daisy.pipeline.tts.css.calabash.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.xmlcalabash.util.TreeWriter;

import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.domassign.StyleMap;

public class CSSInliner {

	private final static String mStyleNsPrefix = "tts";
	private final static String xmlns = "http://www.w3.org/2000/xmlns/";
	private boolean mFirst;
	private TreeWriter mTreeWriter;
	private StyleMap mStyleMap;
	private String mStyleNS;
	private Map<String, String> mPrefixes;
	private static Set<String> mCSS2Properties = new HashSet<String>(Arrays.asList(
	        "voice-family", "stress", "richness", "cue", "cue-before", "cue-after", "pause",
	        "pause-after", "pause-before", "azimuth", "volume", "speak", "play-during",
	        "elevation", "speech-rate", "pitch", "pitch-range", "stress", "speak-punctuation",
	        "speak-numeral", "speak-header"));

	public XdmNode inline(TreeWriterFactory twFactory, URI docURI, XdmNode root,
	        SpeechSheetAnalyser analyzer, String styleNamespace) {
		// match the document with the style sheet
		DocumentOverNodeInfo doc = (DocumentOverNodeInfo) DocumentOverNodeInfo.wrap(root
		        .getUnderlyingNode());

		Document wrapped = (Document) WrapperOverWrapper.wrap(doc);
		mStyleMap = analyzer.evaluateDOM(wrapped);

		mStyleNS = styleNamespace;
		mFirst = true;
		mTreeWriter = twFactory.newInstance();
		mTreeWriter.startDocument(docURI);

		Set<String> namespaces = new HashSet<String>();
		String defaultNs = getAllNamespaces(wrapped.getDocumentElement(), namespaces);
		namespaces.remove(xmlns);
		mPrefixes = new HashMap<String, String>();
		for (String ns : namespaces) {
			String p = "ns" + mPrefixes.size();
			mPrefixes.put(ns, p);
		}
		mPrefixes.put(mStyleNS, "tts");
		mPrefixes.put("http://www.w3.org/XML/1998/namespace", "xml");
		if (defaultNs != null)
			mPrefixes.put(defaultNs, "");

		rebuildRec(wrapped.getOwnerDocument());
		mTreeWriter.endDocument();

		XdmNode result = mTreeWriter.getResult();

		//help the GC does its job
		mStyleNS = null;
		mTreeWriter = null;
		mStyleMap = null;

		return result;
	}

	private static String getAllNamespaces(Node n, Set<String> namespaces) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			String ns = n.getNamespaceURI();
			if (ns != null && !ns.isEmpty())
				namespaces.add(ns);
			NamedNodeMap attributes = n.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				ns = attr.getNamespaceURI();
				if (ns != null && !ns.isEmpty())
					namespaces.add(ns);
			}
			String defaultNs = null;
			for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
				String ret = getAllNamespaces(child, namespaces);
				if (ret != null)
					defaultNs = ret;
			}
			if (n.getPrefix() == null || n.getPrefix().isEmpty())
				return n.getNamespaceURI();
			return defaultNs;
		}
		return null;
	}

	private void rebuildRec(Node node) {
		if (node.getNodeType() == Node.COMMENT_NODE) {
			mTreeWriter.addComment(node.getNodeValue());
		} else if (node.getNodeType() == Node.TEXT_NODE) {
			mTreeWriter.addText(node.getNodeValue());
		} else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
			mTreeWriter.addPI(node.getNodeName(), node.getNodeValue());
		} else if (node.getNodeType() == Node.DOCUMENT_NODE) {
			for (Node child = node.getFirstChild(); child != null; child = child
			        .getNextSibling())
				rebuildRec(child);
		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (node.getNamespaceURI() == null || node.getNamespaceURI().isEmpty())
				mTreeWriter.addStartElement(new QName(null, node.getLocalName()));
			else
				mTreeWriter.addStartElement(new QName(mPrefixes.get(node.getNamespaceURI()),
				        node.getNamespaceURI(), node.getLocalName()));

			if (mFirst) {
				for (Map.Entry<String, String> ns : mPrefixes.entrySet()) {
					mTreeWriter.addNamespace(ns.getValue(), ns.getKey());
				}
				mFirst = false;
			}

			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				if (attr.getNamespaceURI() != null && !attr.getNamespaceURI().isEmpty()) {
					if (!attr.getNamespaceURI().equals(xmlns))
						mTreeWriter.addAttribute(new QName(mPrefixes.get(attr
						        .getNamespaceURI()), attr.getNamespaceURI(), attr
						        .getLocalName()), attr.getNodeValue());
				} else {
					mTreeWriter.addAttribute(new QName(null, attr.getLocalName()), attr
					        .getNodeValue());
				}
			}

			// ===== start inlining ===== //
			NodeData nd = mStyleMap.get((Element) node);
			if (nd != null) {
				for (String property : nd.getPropertyNames()) {
					if (mCSS2Properties.contains(property)) {
						Term<?> t = nd.getValue(property, false);
						String str = null;
						if (t == null || t.getValue() == null) {
							//jStyleParser replaces '-' with '_'. Best workaround so far is to do the opposite:
							//(voice-family and cue aside, there is no property values with '_' in Aural CSS)
							str = nd.getProperty(property, false).toString().replace("_", "-")
							        .toLowerCase();
						} else if (t.getValue() instanceof List<?>) {
							List<?> li = (List<?>) t.getValue();
							StringBuilder sb = new StringBuilder();
							Iterator it = li.iterator();
							sb.append(it.next());
							while (it.hasNext()) {
								Term<?> term = (Term<?>) it.next();
								sb.append("," + term.getValue().toString());
							}
							str = sb.toString();

						} else if (property.startsWith("cue")) {
							str = t.getValue().toString();
						} else {
							str = t.toString().replace("_", "-").toLowerCase();
						}
						mTreeWriter.addAttribute(
						        new QName(mStyleNsPrefix, mStyleNS, property), str);

					}
				}
			}
			mTreeWriter.startContent();

			// ===== end inlining ===== //

			for (Node child = node.getFirstChild(); child != null; child = child
			        .getNextSibling())
				rebuildRec(child);

			mTreeWriter.addEndElement();
		}
	}
}
