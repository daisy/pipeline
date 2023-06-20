package org.daisy.pipeline.css;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Function;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.URIResolver;

import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.NetworkProcessor;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.SourceLocator;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.antlr.CSSSource;
import cz.vutbr.web.csskit.antlr.CSSSourceReader;
import cz.vutbr.web.csskit.antlr.DefaultCSSSourceReader;
import cz.vutbr.web.csskit.antlr.SourceMap;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;
import cz.vutbr.web.csskit.RuleXslt;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.StyleMap;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;

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

	private final String defaultStyleSheets;
	private final MediaSpec medium;
	private final QName attributeName;
	private final CSSParserFactory parserFactory;
	private final RuleFactory ruleFactory;
	private final SupportedCSS supportedCSS;
	private final DeclarationTransformer declarationTransformer;
	private final CSSSourceReader cssReader;
	private final XsltProcessor xsltProcessor;

	private static final Logger logger = LoggerFactory.getLogger(JStyleParserCssCascader.class);

	/**
	 * @param ruleFactory to create StyleSheet objects
	 */
	public JStyleParserCssCascader(URIResolver uriResolver,
	                               CssPreProcessor preProcessor,
	                               XsltProcessor xsltProcessor,
	                               String defaultStyleSheets,
	                               Medium medium,
	                               QName attributeName,
	                               CSSParserFactory parserFactory,
	                               RuleFactory ruleFactory,
	                               SupportedCSS supportedCSS,
	                               DeclarationTransformer declarationTransformer) {
		this.defaultStyleSheets = defaultStyleSheets;
		this.medium = medium.asMediaSpec();
		this.attributeName = attributeName;
		this.parserFactory = parserFactory;
		this.ruleFactory = ruleFactory;
		this.supportedCSS = supportedCSS;
		this.declarationTransformer = declarationTransformer;
		this.xsltProcessor = xsltProcessor;
		NetworkProcessor network = new DefaultNetworkProcessor() {
				@Override
				public Reader fetch(URL url, Charset encoding, boolean forceEncoding, boolean assertEncoding) throws IOException {
					logger.debug("Fetching style sheet: " + url);
					Source resolved; {
						try {
							resolved = uriResolver.resolve(URLs.asURI(url).toASCIIString(), ""); }
						catch (javax.xml.transform.TransformerException e) {
							throw new IOException(e); }}
					if (resolved != null && resolved instanceof StreamSource) {
						InputStreamReader r = detectEncodingAndSkipBOM(
							((StreamSource)resolved).getInputStream(), null, encoding, forceEncoding);
						if (assertEncoding) {
							if (encoding == null)
								throw new IllegalArgumentException("encoding must not be null");
							if (!encoding.equals(getEncoding(r)))
								throw new IOException("Failed to read URL as " + encoding + ": " + url);
						}
						return r;
					} else {
						if (resolved != null)
							url = new URL(resolved.getSystemId());
						return super.fetch(url, encoding, forceEncoding, assertEncoding);
					}
				}
			};
		/*
		 * CSSSourceReader that handles media types supported by preProcessor. Throws a
		 * IOException if something goes wrong when resolving the source or if the
		 * pre-processing fails.
		 */
		this.cssReader = new DefaultCSSSourceReader(network) {
				@Override
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
				@Override
				public CSSInputStream read(CSSSource source) throws IOException {
					if (source.type == CSSSource.SourceType.URL) {
						try {
							Source resolved = uriResolver.resolve(URLs.asURI((URL)source.source).toASCIIString(), "");
							if (resolved != null)
								source = new CSSSource(new URL(resolved.getSystemId()), source.encoding, source.mediaType);
						} catch (javax.xml.transform.TransformerException e) {
							throw new IOException(e);
						}
					}
					CSSInputStream cssStream = super.read(source);
					if (!("text/css".equals(source.mediaType)
					      || source.mediaType == null && (source.type != CSSSource.SourceType.URL
					                                      || ((URL)source.source).toString().endsWith(".css")))) {
						// preProcessor must be non-null
						try {
							CssPreProcessor.PreProcessingResult result = preProcessor.compile(
								new CssPreProcessor.PreProcessingSource(cssStream.stream, URLs.asURI(cssStream.base)) {
									@Override
									public Reader reread(Charset encoding) throws IOException {
										Reader r = cssStream.reread(encoding);
										stream.close();
										return r;
									}
								}
							);
							SourceMap sourceMap; {
								if (result.sourceMap != null) {
									SourceMap m = SourceMapReader.read(result.sourceMap, result.base);
									if (cssStream.sourceMap != null) {
										sourceMap = new SourceMap() {
											public SourceLocator get(int line, int column) {
												SourceLocator loc = m.get(line, column);
												if (loc != null && loc.getURL().equals(cssStream.base))
													loc = cssStream.sourceMap.get(loc.getLineNumber(), loc.getColumnNumber());
												return loc;
											}
											public SourceLocator floor(int line, int column) {
												SourceLocator loc = m.floor(line, column);
												if (loc != null && loc.getURL().equals(cssStream.base))
													loc = cssStream.sourceMap.floor(loc.getLineNumber(), loc.getColumnNumber());
												return loc;
											}
											public SourceLocator ceiling(int line, int column) {
												SourceLocator loc = m.ceiling(line, column);
												if (loc != null && loc.getURL().equals(cssStream.base))
													loc = cssStream.sourceMap.ceiling(loc.getLineNumber(), loc.getColumnNumber());
												return loc;
											}
										};
									} else
										sourceMap = m;
								} else
									sourceMap = cssStream.sourceMap;
							}
							String resultString = CharStreams.toString(result.stream);
							return new CSSInputStream(new StringReader(resultString), cssStream.base, sourceMap) {
								@Override
								public Reader reread(Charset encoding) throws IOException {
									// assuming that the preprocessor has already handled @charset rules
									// simply return the remainder of the stream
									result.stream.close();
									return new StringReader(resultString);
								}
							};
						} catch (RuntimeException e) {
							throw new IOException(
								(source.mediaType != null ? (source.mediaType + " p") : "P")
								+ "re-processing failed: " + e.getMessage(), e);
						}
					} else
						return cssStream;
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
			Function<Node,SourceLocator> nodeLocator = n -> {
				if (n instanceof NodeOverNodeInfo) {
					NodeInfo info = ((NodeOverNodeInfo)n).getUnderlyingNodeInfo();
					return new SourceLocator() {
						public URL getURL() {
							return URLs.asURL(URI.create(info.getBaseURI()));
						}
						public int getLineNumber() {
							return info.getLineNumber();
						}
						public int getColumnNumber() {
							return info.getColumnNumber();
						}
					};
				} else {
					return new SourceLocator() {
						public URL getURL() {
							return URLs.asURL(URI.create(n.getBaseURI()));
						}
						public int getLineNumber() {
							return 0;
						}
						public int getColumnNumber() {
							return 0;
						}
					};
				}
			};
			StyleMap styleMap;
			StyleSheet defaultStyleSheet; {
				StyleSheet s = (StyleSheet)ruleFactory.createStyleSheet().unlock();
				if (defaultStyleSheets != null) {
					StringTokenizer t = new StringTokenizer(defaultStyleSheets);
					while (t.hasMoreTokens()) {
						URL u = URLs.asURL(URLs.resolve(baseURI, URLs.asURI(t.nextToken())));
						if (!cssReader.supportsMediaType(null, u))
							logger.warn("Style sheet type not supported: " + u);
						else
							s = parserFactory.append(new CSSSource(u, (Charset)null, (String)null), cssReader, s);
					}
				}
				defaultStyleSheet = s;
			}
			styleSheet = (StyleSheet)ruleFactory.createStyleSheet().unlock();
			styleSheet.addAll(defaultStyleSheet);
			synchronized(JStyleParserCssCascader.class) {
				// FIXME: CSSParserFactory injected in CSSAssignTraversal.<init> in CSSFactory.getUsedStyles
				CSSFactory.registerCSSParserFactory(parserFactory);
				styleSheet = CSSFactory.getUsedStyles(document, nodeLocator, medium, cssReader, styleSheet);
			}

			// FIXME: use a dedicated parser to parse @xslt rules (and ignore all the rest)
			XMLInputValue<?> transformed = null;
			for (RuleXslt r : Iterables.filter(styleSheet, RuleXslt.class)) {
				Map<QName,InputValue<?>> params = new HashMap<>();
				for (Declaration d : r) {
					List<Object> val = new ArrayList<>();
					boolean invalid = false;
					for (Term<?> t : d) {
						if (t instanceof TermIdent || t instanceof TermString) {
							String v = ((Term<String>)t).getValue();
							if (t instanceof TermIdent && ("true".equals(v) || "false".equals(v)))
								val.add(Boolean.valueOf(v));
							else
								val.add(v);
						} else if (t instanceof TermInteger)
							val.add(((TermInteger)t).getIntValue());
						else {
							logger.warn("@xslt parameter value must be a sequence of string, ident or integer, but got " + d);
							invalid = true;
							break;
						}
					}
					if (!invalid)
						params.put(new QName(d.getProperty()), new InputValue<>(val));
				}
				Document doc; {
					if (transformed != null) {
						Mult<? extends XMLInputValue<?>> m = transformed.mult(2);
						doc = (Document)m.get().ensureSingleItem().asNodeIterator().next();
						transformed = m.get();
					} else
						doc = document;
				}
				params.put(
					new QName("style"),
					new InputValue<>(
						new StyleAccessor() {
							StyleMap style = null;
							public Optional<String> get(Element element, String property) {
								if (style == null) {
									StyleSheet s = (StyleSheet)ruleFactory.createStyleSheet().unlock();
									s.addAll(defaultStyleSheet);
									synchronized(JStyleParserCssCascader.class) {
										// FIXME: CSSParserFactory injected in CSSAssignTraversal.<init> in CSSFactory.getUsedStyles
										CSSFactory.registerCSSParserFactory(parserFactory);
										// not using element.getOwnerDocument() because base URI is null/empty in some cases
										s = CSSFactory.getUsedStyles(doc, nodeLocator, medium, cssReader, s);
									}
									style = new Analyzer(s, declarationTransformer, supportedCSS)
									            .evaluateDOM(doc, medium, true); }
								NodeData data = style.get(element);
								if (data != null) {
									Term<?> value = data.getValue(property, true);
									if (value != null)
										return Optional.of(serializeValue(value));
									else {
										CSSProperty p = data.getProperty(property);
										if (p != null)
											return Optional.of(p.toString()); }}
								return Optional.empty();
							}

							Map<String,List<CombinedSelector>> selectorCache = null; // cache of compiled selectors
							public boolean matches(Element element, String selector) {
								List<CombinedSelector> compiledSelector;
								if (selectorCache == null)
									selectorCache = new HashMap<>();
								if (selectorCache.containsKey(selector))
									compiledSelector = selectorCache.get(selector);
								else {
									compiledSelector = parserFactory.parseSelector(selector, r.namespaces);
									if (compiledSelector != null)
										selectorCache.put(selector, compiledSelector); }
								if (compiledSelector == null)
									return false;
								for (CombinedSelector sel : compiledSelector) {
									boolean match = false;
									Combinator combinator = null;
									for (int i = sel.size() - 1; i >= 0; i--) {
										Selector s = sel.get(i);
										if (combinator == null)
											match = s.matches(element);
										else if (combinator == Combinator.ADJACENT) {
											Node adjacent = element.getPreviousSibling();
											while (adjacent != null && !(adjacent instanceof Element))
												adjacent = adjacent.getPreviousSibling();
											match = adjacent != null && s.matches((Element)adjacent); }
										else if (combinator == Combinator.PRECEDING) {
											match = false;
											Node preceding = element.getPreviousSibling();
											while (!match) {
												while (preceding != null && !(preceding instanceof Element))
													preceding = preceding.getPreviousSibling();
												if (preceding == null)
													break;
												match = s.matches((Element)preceding);
												preceding = preceding.getPreviousSibling(); }}
										else if (combinator == Combinator.DESCENDANT) {
											match = false;
											Node ancestor = element.getParentNode();
											while (!match && ancestor != null) {
												match = s.matches((Element)ancestor);
												ancestor = ancestor.getParentNode(); }}
										else if (combinator == Combinator.CHILD) {
											Element parent = (Element)element.getParentNode();
											match = parent != null && s.matches(parent); }
										combinator = s.getCombinator();
										if (!match)
											return false;
									}
									if (match == true)
										return true;
								}
								return false;
							}
						}
					)
				);
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
				styleSheet = (StyleSheet)ruleFactory.createStyleSheet().unlock();
				styleSheet.addAll(defaultStyleSheet);
				synchronized(JStyleParserCssCascader.class) {
					// FIXME: CSSParserFactory injected in CSSAssignTraversal.<init> in CSSFactory.getUsedStyles
					CSSFactory.registerCSSParserFactory(parserFactory);
					styleSheet = CSSFactory.getUsedStyles(document, nodeLocator, medium, cssReader, styleSheet);
				}
			}
			styleMap = new Analyzer(styleSheet, declarationTransformer, supportedCSS).evaluateDOM(document, medium, false);
			writer.setBaseURI(baseURI);
			writer.writeStartDocument();
			traverse(document.getDocumentElement(), styleMap, writer);
			writer.writeEndDocument();
		} catch (TransformerException e) {
			throw e;
		} catch (Exception e) {
			throw new TransformerException(e);
		} finally {
			styleSheet = null;
		}
	}

	protected abstract String serializeStyle(NodeData mainStyle, Map<PseudoElement,NodeData> pseudoStyles, Element context);

	protected abstract String serializeValue(Term<?> value);

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
