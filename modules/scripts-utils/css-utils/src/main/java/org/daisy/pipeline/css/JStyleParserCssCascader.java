package org.daisy.pipeline.css;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.URIResolver;

import com.google.common.collect.Iterators;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.NetworkProcessor;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.antlr.CSSParserFactory.SourceType;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.StyleMap;

import org.apache.commons.io.input.BOMInputStream;

import org.daisy.common.file.URIs;
import org.daisy.common.file.URLs;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeCharacters;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeComment;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeProcessingInstruction;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class JStyleParserCssCascader extends SingleInSingleOutXMLTransformer {

	private final String defaultStyleSheet;
	private final String medium;
	private final QName attributeName;
	private final CSSParserFactory parserFactory;
	private final RuleFactory ruleFactory;
	private final SupportedCSS supportedCSS;
	private final DeclarationTransformer declarationTransformer;
	private final NetworkProcessor network;

	private static final Logger logger = LoggerFactory.getLogger(JStyleParserCssCascader.class);

	public JStyleParserCssCascader(URIResolver uriResolver,
	                               String defaultStyleSheet,
	                               String medium,
	                               QName attributeName,
	                               CSSParserFactory parserFactory,
	                               RuleFactory ruleFactory,
	                               SupportedCSS supportedCSS,
	                               DeclarationTransformer declarationTransformer) {
		this.defaultStyleSheet = defaultStyleSheet;
		this.medium = medium;
		this.attributeName = attributeName;
		this.parserFactory = parserFactory;
		this.ruleFactory = ruleFactory;
		this.supportedCSS = supportedCSS;
		this.declarationTransformer = declarationTransformer;
		this.network = new DefaultNetworkProcessor() {
				@Override
				public InputStream fetch(URL url) throws IOException {
					logger.debug("Fetching CSS style sheet: " + url);
					InputStream is; {
						Source resolved; {
							try {
								resolved = uriResolver.resolve(URIs.asURI(url).toString(), ""); }
							catch (javax.xml.transform.TransformerException e) {
								throw new IOException(e); }}
						if (resolved != null && resolved instanceof StreamSource)
							is = ((StreamSource)resolved).getInputStream();
						else {
							if (resolved != null) {
								url = new URL(resolved.getSystemId());
								logger.debug("Resolved to :" + url); }
							is = super.fetch(url); }}
					// skip BOM
					is = new BOMInputStream(is);
					return is;
				}
			};
	}

	private StyleSheet styleSheet = null;

	public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) {
		if (source == null || result == null)
			throw new IllegalArgumentException();
		return () -> transform(source.ensureSingleItem().asNodeIterator().next(), result.asXMLStreamWriter());
	}

	private void transform(Node node, BaseURIAwareXMLStreamWriter output) throws TransformerException {
		if (!(node instanceof Document))
			throw new TransformerException(new IllegalArgumentException());
		Document document = (Document)node;
		BaseURIAwareXMLStreamWriter writer = output;
		try {
			URI baseURI = new URI(document.getBaseURI());
			URL baseURL; {
				if (baseURI == null || baseURI.toString().equals(""))
					// handling the case where base URI is empty, although this should in theory never happen
					// FIXME: find out why it happens
					baseURL = null;
				else
					baseURL = URLs.asURL(baseURI);
			}
			StyleMap styleMap;
			synchronized(JStyleParserCssCascader.class) {
				// CSSParserFactory injected in CSSAssignTraversal.<init> in CSSFactory.getUsedStyles
				CSSFactory.registerCSSParserFactory(parserFactory);
				// RuleFactory injected in
				// - SimplePreparator.<init> in CSSParserFactory.append in CSSFactory.getUsedStyles
				// - CSSTreeParser.<init> in CSSParserFactory.append in CSSFactory.getUsedStyles
				CSSFactory.registerRuleFactory(ruleFactory);
				// DeclarationTransformer injected in SingleMapNodeData.<init> in CSSFactory.createNodeData in Analyzer.evaluateDOM
				CSSFactory.registerDeclarationTransformer(declarationTransformer);
				// SupportedCSS injected in
				// - SingleMapNodeData.<init> in CSSFactory.createNodeData in Analyzer.evaluateDOM
				// - Repeater.assignDefaults in DeclarationTransformer.parseDeclaration in SingleMapNodeData.push in Analyzer.evaluateDOM
				// - Variator.assignDefaults in DeclarationTransformer.parseDeclaration in SingleMapNodeData.push in Analyzer.evaluateDOM
				CSSFactory.registerSupportedCSS(supportedCSS);
				styleSheet = (StyleSheet)ruleFactory.createStyleSheet().unlock();
				if (defaultStyleSheet != null) {
					StringTokenizer t = new StringTokenizer(defaultStyleSheet);
					while (t.hasMoreTokens()) {
						URL u = URLs.asURL(baseURI.resolve(URIs.asURI(t.nextToken())));
						styleSheet = parserFactory.append(u, network, null, SourceType.URL, styleSheet, u);
					}
				}
				styleSheet = CSSFactory.getUsedStyles(document, null, baseURL, new MediaSpec(medium), network, styleSheet);
				styleMap = new Analyzer(styleSheet).evaluateDOM(document, medium, false);
			}
			writer.setBaseURI(baseURI);
			writer.writeStartDocument();
			traverse(document.getDocumentElement(), styleMap, writer);
			writer.writeEndDocument();
		} catch (Exception e) {
			throw new TransformerException(e);
		} finally {
			styleSheet = null;
		}
	}

	protected abstract String serializeStyle(NodeData mainStyle, Map<PseudoElement,NodeData> pseudoStyles, Element context);

	protected StyleSheet getParsedStyleSheet() {
		if (styleSheet == null)
			throw new UnsupportedOperationException();
		return styleSheet;
	}

	private void traverse(Node node, StyleMap styleMap, BaseURIAwareXMLStreamWriter writer) throws XMLStreamException {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element)node;
			writeStartElement(writer, elem);
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attr = attributes.item(i);
				if (!(attr.getPrefix() == null && "style".equals(attr.getLocalName())))
					writeAttribute(writer, attr); }
			String style = serializeStyle(
				styleMap.get(elem),
				new AbstractMap<PseudoElement,NodeData>() {
					Set<Entry<PseudoElement,NodeData>> entrySet = new AbstractSet<Entry<PseudoElement,NodeData>>() {
							Set<PseudoElement> keySet = styleMap.pseudoSet(elem);
							public Iterator<Entry<PseudoElement,NodeData>> iterator() {
								return Iterators.transform(
									keySet.iterator(),
									pseudo -> new SimpleImmutableEntry<PseudoElement,NodeData>(
										pseudo, styleMap.get(elem, pseudo)));
							}
							public int size() {
								return keySet.size();
							}
						};
					public Set<Entry<PseudoElement,NodeData>> entrySet() {
						return entrySet;
					}
				},
				elem);
			if (style != null)
				writeAttribute(writer, attributeName, style);
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
				traverse(child, styleMap, writer);
			writer.writeEndElement(); }
		else if (node.getNodeType() == Node.COMMENT_NODE)
			writeComment(writer, node);
		else if (node.getNodeType() == Node.TEXT_NODE)
			writeCharacters(writer, node);
		else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
			writeProcessingInstruction(writer, node);
		else
			throw new UnsupportedOperationException("Unexpected node type");
	}
}
