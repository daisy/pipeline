package org.daisy.pipeline.css;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.URIResolver;

import com.google.common.collect.Iterables;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.NetworkProcessor;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.CSSSourceReader;
import cz.vutbr.web.csskit.antlr.SourceMap;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;
import cz.vutbr.web.csskit.RuleXslt;
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
import org.daisy.common.transform.Mult;
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
	private final XsltProcessor xsltProcessor;

	private static final Logger logger = LoggerFactory.getLogger(JStyleParserCssCascader.class);

	public JStyleParserCssCascader(URIResolver uriResolver,
	                               CssPreProcessor preProcessor,
	                               XsltProcessor xsltProcessor,
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
		this.xsltProcessor = xsltProcessor;
		NetworkProcessor defaultNetwork = new DefaultNetworkProcessor();
		/*
		 * CSSSourceReader that handles media types supported by preProcessor. Throws a
		 * IOException if something goes wrong when resolving the source or if the
		 * pre-processing fails.
		 */
		this.cssReader = new CSSSourceReader() {
				public boolean supportsMediaType(String mediaType, URL url) {
					if ("text/css".equals(mediaType))
						return true;
					else if (mediaType == null && (url == null || url.toString().endsWith(".css")))
						return true;
					else if (preProcessor == null)
						return false;
					else
						return preProcessor.supportsMediaType(mediaType, url);
				}
				public CSSInputStream read(CSSSource source) throws IOException {
					URL url = null;
					InputStream is;
					SourceMap sourceMap = null; {
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
					URL base = url != null ? url : source.base;
					if (!supportsMediaType(source.mediaType, url))
						throw new IllegalArgumentException();
					if (!("text/css".equals(source.mediaType)
					      || source.mediaType == null && (url == null || url.toString().endsWith(".css")))) {
						// preProcessor must be non-null
						try {
							CssPreProcessor.PreProcessingResult result
								= preProcessor.compile(is, url != null ? url : source.base, source.encoding);
							is = result.stream;
							if (result.sourceMap != null)
								sourceMap = SourceMapReader.read(result.sourceMap, result.base);
						} catch (RuntimeException e) {
							throw new IOException(
								(source.mediaType != null ? (source.mediaType + " p") : "P")
								+ "re-processing failed: " + e.getMessage(), e);
						}
					}
					return new CSSInputStream(is, source.encoding, base, sourceMap);
				}
			};
	}

	private StyleSheet styleSheet = null;

	public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws TransformerException {
		if (source == null || result == null)
			throw new TransformerException(new IllegalArgumentException());
		return () -> transform(source.ensureSingleItem().mult(2), result.asXMLStreamWriter());
	}

	private void transform(Mult<? extends XMLInputValue<?>> source, BaseURIAwareXMLStreamWriter output) throws TransformerException {
		Node node = source.get().asNodeIterator().next();
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
				XMLInputValue<?> transformed = null;
				for (RuleXslt r : Iterables.filter(styleSheet, RuleXslt.class)) {
					Map<String,String> params = new HashMap<>();
					for (Declaration d: r) {
						if (d.size() == 1) {
							if (d.get(0) instanceof TermIdent || d.get(0) instanceof TermString)
								params.put(d.getProperty(), ((Term<String>)d.get(0)).getValue());
							else if (d.get(0) instanceof TermInteger)
								params.put(d.getProperty(), ""+((TermInteger)d.get(0)).getIntValue());
							else
								logger.warn("@xslt parameter value must be a string, ident or integer, but got " + d);
						} else
							logger.warn("@xslt parameter value must consist of exactly one part, but got " + d);
					}
					transformed = xsltProcessor.transform(
						URLs.resolve(URLs.asURI(r.base), URLs.asURI(r.uri)),
						transformed != null ? transformed : source.get(),
						params);
				}
				if (transformed != null) {
					node = transformed.ensureSingleItem().asNodeIterator().next();
					if (!(node instanceof Document))
						throw new TransformerException(
							new RuntimeException("XsltProcessor must return a (single) document"));
					document = (Document)node;
					// We assume that base URI did not change.
					// We need to recompute the stylesheet because of any possible inline styles, which
					// are attached to an element in the original document.
					styleSheet = CSSFactory.getUsedStyles(document, null, baseURL, medium, cssReader, styleSheet);
				}
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
			Map<PseudoElement,NodeData> pseudoStyles = new java.util.HashMap<>(); {
				for (PseudoElement pseudo : styleMap.pseudoSet(elem))
					pseudoStyles.put(pseudo, styleMap.get(elem, pseudo)); }
			String style = serializeStyle(
				styleMap.get(elem),
				pseudoStyles,
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
