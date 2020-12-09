package org.daisy.pipeline.css;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.nio.charset.Charset;
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
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.CSSSourceReader;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.StyleMap;

import org.apache.commons.io.input.BOMInputStream;

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
	private final MediaSpec medium;
	private final QName attributeName;
	private final CSSParserFactory parserFactory;
	private final RuleFactory ruleFactory;
	private final SupportedCSS supportedCSS;
	private final DeclarationTransformer declarationTransformer;
	private final CSSSourceReader cssReader;

	private static final Logger logger = LoggerFactory.getLogger(JStyleParserCssCascader.class);

	public JStyleParserCssCascader(URIResolver uriResolver,
	                               SassCompiler sassCompiler,
	                               String defaultStyleSheet,
	                               Medium medium,
	                               QName attributeName,
	                               CSSParserFactory parserFactory,
	                               RuleFactory ruleFactory,
	                               SupportedCSS supportedCSS,
	                               DeclarationTransformer declarationTransformer) {
		this.defaultStyleSheet = defaultStyleSheet;
		this.medium = medium.asMediaSpec();
		this.attributeName = attributeName;
		this.parserFactory = parserFactory;
		this.ruleFactory = ruleFactory;
		this.supportedCSS = supportedCSS;
		this.declarationTransformer = declarationTransformer;
		NetworkProcessor defaultNetwork = new DefaultNetworkProcessor();
		/*
		 * CSSSourceReader that handles media type "text/x-scss". Throws a IOException if something
		 * goes wrong when resolving the source or if the SASS compilation fails.
		 */
		this.cssReader = new CSSSourceReader() {
				public boolean supportsMediaType(String mediaType, URL url) {
					if ("text/css".equals(mediaType))
						return true;
					else if (mediaType == null && (url == null || url.toString().endsWith(".css")))
						return true;
					else if (sassCompiler == null)
						return false;
					else if ("text/x-scss".equals(mediaType))
						return true;
					else if (mediaType == null && url != null && url.toString().endsWith(".scss"))
						return true;
					else
						return false;
				}
				public CSSInputStream read(CSSSource source) throws IOException {
					URL url = null;
					InputStream is; {
						switch (source.type) {
						case INLINE:
						case EMBEDDED:
							is = new ByteArrayInputStream(((String)source.source).getBytes());
							break;
						case URL:
							url = (URL)source.source;
							logger.debug("Fetching style sheet: " + url);
							Source resolved; {
								try {
									resolved = uriResolver.resolve(URLs.asURI(url).toASCIIString(), ""); }
								catch (javax.xml.transform.TransformerException e) {
									throw new IOException(e); }}
							if (resolved != null) {
								url = new URL(resolved.getSystemId());
								logger.debug("Resolved to :" + url); }
							if (resolved != null && resolved instanceof StreamSource)
								is = ((StreamSource)resolved).getInputStream();
							else
								is = defaultNetwork.fetch(url);
							// skip BOM
							is = new BOMInputStream(is);
							break;
						default:
							throw new RuntimeException("coding error");
						}
					}
					if (!supportsMediaType(source.mediaType, url))
						throw new IllegalArgumentException();
					if ("text/x-scss".equals(source.mediaType)
					    || (source.mediaType == null && url != null && url.toString().endsWith(".scss"))) {
						// sassCompiler must be non-null
						try {
							is = sassCompiler.compile(is, url != null ? url : source.base, source.encoding);
						} catch (RuntimeException e) {
							throw new IOException("SASS compilation failed", e);
						}
					}
					// FIXME: there should be a way to pass the resolved URL to the CSSInputStream
					// so that relative imports can be handled correctly
					return new CSSInputStream(is, source.encoding);
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
						URL u = URLs.asURL(URLs.resolve(baseURI, URLs.asURI(t.nextToken())));
						if (!cssReader.supportsMediaType(null, u))
							logger.warn("Style sheet type not supported: " + u);
						else
							styleSheet = parserFactory.append(new CSSSource(u, (Charset)null, (String)null), cssReader, styleSheet);
					}
				}
				styleSheet = CSSFactory.getUsedStyles(document, null, baseURL, medium, cssReader, styleSheet);
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
