package org.daisy.pipeline.braille.css.calabash.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.URIResolver;

import com.google.common.base.Function;
import static com.google.common.base.MoreObjects.firstNonNull;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterators.forArray;
import com.google.common.io.ByteSource;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.Base64;
import com.xmlcalabash.util.S9apiUtils;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.NetworkProcessor;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermNumber;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.antlr.CSSParserFactory.SourceType;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;
import cz.vutbr.web.csskit.RuleFactoryImpl;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.StyleMap;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.io.input.BOMInputStream;

import org.daisy.braille.css.AnyAtRule;
import org.daisy.braille.css.BrailleCSSDeclarationTransformer;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSProperty;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;

import org.daisy.common.calabash.XMLCalabashHelper;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeCharacters;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeComment;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeProcessingInstruction;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.transform.DOMToXMLStreamTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.Strings.normalizeSpace;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import static org.daisy.pipeline.braille.common.util.URLs.asURL;
import org.daisy.pipeline.braille.css.SupportedPrintCSS;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssInlineStep extends DefaultStep {
	
	private ReadablePipe sourcePipe = null;
	private ReadablePipe contextPipe = null;
	private WritablePipe resultPipe = null;
	private Map<String,String> sassVariables = new HashMap<String,String>();
	private NetworkProcessor network = null;
	private final InMemoryURIResolver inMemoryResolver;
	private Importer importer = null;
	
	private final String scssNumber = "\\d*\\.\\d+";
	private final String scssColor = "(#[\\da-zA-Z]+|(rgb|hsl)a?\\([^)]*\\))";
	private final String scssBadStringChars = "!\"#$'()*+,\\.\\/:<=>?@\\[\\\\\\]^`{|}~-";
	private final String scssNumberColorString = "\\s*("+ scssNumber
	                                                +"|"+ scssColor
	                                                +"|[^\\s"+scssBadStringChars+"]+"
	                                                +"|\"([^\"]|\\\")*\""
	                                                +"|'([^']|\\')*'"
	                                                +")\\s*";
	
	private static final QName _default_stylesheet = new QName("default-stylesheet");
	private static final QName _media = new QName("media");
	private static final QName _attribute_name = new QName("attribute-name");
	
	private static final String DEFAULT_MEDIUM = "embossed";
	private static final QName DEFAULT_ATTRIBUTE_NAME = new QName("style");
	
	private CssInlineStep(XProcRuntime runtime, XAtomicStep step, final URIResolver resolver) {
		super(runtime, step);
		inMemoryResolver = new InMemoryURIResolver();
		final URIResolver _resolver = fallback(inMemoryResolver, resolver);
		importer = new Importer() {
			public Collection<Import> apply(String url, Import previous) {
				try {
					URI uri = asURI(url);
					URI base = previous.getAbsoluteUri();
					logger.debug("Importing SASS style sheet: " + uri + " (base = " + base + ")");
					URI abs = base.resolve(uri);
					InputStream is; {
						Source resolved; {
							try {
								resolved = _resolver.resolve(abs.toString(), ""); }
							catch (javax.xml.transform.TransformerException e) {
								throw new IOException(e); }}
						if (resolved != null && resolved instanceof StreamSource)
							is = ((StreamSource)resolved).getInputStream();
						else {
							if (resolved != null) {
								abs = asURI(resolved.getSystemId());
								logger.debug("Resolved to: " + abs); }
							is = asURL(abs).openStream(); }}
					try {
						return ImmutableList.of(
							new Import(uri, abs,
							           byteSource(is).asCharSource(StandardCharsets.UTF_8).read())); }
					catch (RuntimeException e) {
						throw new IOException(e); }}
				catch (IOException e) {
					if (!url.endsWith(".scss"))
						return apply(url + ".scss", previous);
					else
						throw new RuntimeException(e); }
			}
		};
		network = new DefaultNetworkProcessor() {
			@Override
			public InputStream fetch(URL url) throws IOException {
				logger.debug("Fetching CSS style sheet: " + url);
				InputStream is; {
					Source resolved; {
						try {
							resolved = _resolver.resolve(asURI(url).toString(), ""); }
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
				if (url.toString().endsWith(".scss")) {
					Compiler sassCompiler = new Compiler();
					Options options = new Options();
					options.setIsIndentedSyntaxSrc(false);
					options.setOutputStyle(OutputStyle.EXPANDED);
					options.setSourceMapContents(false);
					options.setSourceMapEmbed(false);
					options.setSourceComments(false);
					options.setPrecision(5);
					options.setOmitSourceMapUrl(true);
					options.getImporters().add(importer);
					String scss = "";
					for (String var : sassVariables.keySet()) {
						String value = sassVariables.get(var);
						if (!value.matches(scssNumberColorString)) {
							// if value contains spaces or special characters that can mess up parsing; wrap it in single quotes
							logger.debug("scss variable '"+var+"' contains special characters: "+value);
							value = "'"+value.replaceAll("'", "\\\\'")+"'";
							logger.debug("scss variable '"+var+"' was quoted                 : "+value);
						} else {
							logger.debug("scss variable '"+var+"' contains no special characters: "+value);
						}
						scss += ("$" + var + ": " + value + ";\n");
					}
					scss += byteSource(is).asCharSource(StandardCharsets.UTF_8).read();
					try {
						Output result = sassCompiler.compileString(scss, StandardCharsets.UTF_8, asURI(url), null, options);
						if (result.getErrorStatus() != 0)
							throw new IOException("Could not compile SASS style sheet: " + result.getErrorMessage());
						String css = result.getCss();
						logger.debug(url + " compiled to:\n\n" + css);
						return new ByteArrayInputStream(css.getBytes(StandardCharsets.UTF_8)); }
					catch (CompilationException e) {
						throw new IOException("Could not compile SASS style sheet", e); }}
				else
					return is;
			}
		};
	}
	
	private static ByteSource byteSource(final InputStream is) {
		return new ByteSource() {
			public InputStream openStream() throws IOException {
				return is;
			}
		};
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		if ("source".equals(port))
			sourcePipe = pipe;
		else
			contextPipe = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		resultPipe = pipe;
	}
	
	@Override
	public void setParameter(String port, QName name, RuntimeValue value) {
		if ("sass-variables".equals(port))
			if ("".equals(name.getNamespaceURI())) {
				sassVariables.put(name.getLocalName(), value.getString());
				return; }
		super.setParameter(port, name, value);
	}
	
	@Override
	public void setParameter(QName name, RuntimeValue value) {
		
		// Calabash calls this function and never setParameter(String port,
		// ...) so I just have to assume that port is "sass-variables"
		setParameter("sass-variables", name, value);
	}
	
	@Override
	public void reset() {
		sourcePipe.resetReader();
		resultPipe.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			String medium = getOption(_media, DEFAULT_MEDIUM);
			QName attributeName = getOption(_attribute_name, DEFAULT_ATTRIBUTE_NAME);
			inMemoryResolver.setContext(contextPipe);
			XMLCalabashHelper.transform(
				new CssInlineTransformer(network, getOption(_default_stylesheet, ""), medium, SaxonHelper.jaxpQName(attributeName)),
				sourcePipe,
				resultPipe,
				runtime); }
		catch (Exception e) {
			logger.error("css:inline failed", e);
			throw new XProcException(step.getNode(), e); }
	}
	
	private static final QName c_encoding = new QName("c", XProcConstants.NS_XPROC_STEP, "encoding");
	private static final QName _content_type = new QName("content-type");
	
	static class InMemoryURIResolver implements URIResolver {
		private ReadablePipe documents = null;
		InMemoryURIResolver() {}
		void setContext(ReadablePipe documents) {
			this.documents = documents;
		}
		public Source resolve(String href, String base) throws TransformerException {
			if (documents == null) return null;
			try {
				URI uri = (base != null) ?
					new URI(base).resolve(new URI(href)) :
					new URI(href);
				uri = normalizeUri(uri);
				documents.resetReader();
				while (documents.moreDocuments()) {
					XdmNode doc = documents.read();
					URI docUri = normalizeUri(doc.getBaseURI());
					if (docUri.equals(uri)) {
						XdmNode root = S9apiUtils.getDocumentElement(doc);
						if (XProcConstants.c_result.equals(root.getNodeName())
						    && root.getAttributeValue(_content_type) != null
						    && root.getAttributeValue(_content_type).startsWith("text/"))
							return new StreamSource(new ByteArrayInputStream(doc.getStringValue().getBytes()),
							                        uri.toASCIIString());
						else if ("base64".equals(root.getAttributeValue(c_encoding)))
							return new StreamSource(new ByteArrayInputStream(Base64.decode(doc.getStringValue())),
							                        uri.toASCIIString());
						else if (XProcConstants.c_data.equals(root.getNodeName()))
							return new StreamSource(new ByteArrayInputStream(doc.getStringValue().getBytes()),
							                        uri.toASCIIString());
						else
							return doc.asSource(); }}}
			catch (URISyntaxException e) {
				e.printStackTrace();
				throw new TransformerException(e); }
			catch (SaxonApiException e) {
				e.printStackTrace();
				throw new TransformerException(e); }
			return null;
		}
	}
	
	private static URI normalizeUri(URI uri) {
		String s = uri.toASCIIString();
		
		// A URI that starts with "file:///" can be written as "file:/"
		if (s.startsWith("file:///"))
			s = "file:" + s.substring(7);
		
		// A URI that contains "!/" is a ZIP URI
		if (s.startsWith("file:") && s.contains("!/"))
			s = "zip:" + s;
		
		// The part of a ZIP URI after the "!" must start with "/"
		if (s.startsWith("zip:file:") && !s.contains("!/"))
			s = s.replaceFirst("!", "!/");
		try {
			return new URI(s); }
		catch (URISyntaxException e) {
			throw new RuntimeException(e); }
	}
	
	private static URIResolver fallback(final URIResolver... resolvers) {
		return new URIResolver() {
			public Source resolve(String href, String base) throws javax.xml.transform.TransformerException {
				Iterator<URIResolver> iterator = forArray(resolvers);
				while (iterator.hasNext()) {
					Source source = iterator.next().resolve(href, base);
					if (source != null)
						return source; }
				return null;
			}
		};
	}
	
	@Component(
		name = "css:inline",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}css-inline" }
	)
	public static class Provider implements XProcStepProvider {
		
		private URIResolver resolver;
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new CssInlineStep(runtime, step, resolver);
		}
		
		@Reference(
			name = "URIResolver",
			unbind = "-",
			service = URIResolver.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		public void setUriResolver(URIResolver resolver) {
			this.resolver = resolver;
		}
	}
	
	// medium print
	private static final SupportedCSS printCSS = SupportedPrintCSS.getInstance();
	private static DeclarationTransformer printDeclarationTransformer; static {
		// SupportedCSS injected via CSSFactory in DeclarationTransformer.<init>
		CSSFactory.registerSupportedCSS(printCSS);
		printDeclarationTransformer = new DeclarationTransformer() {}; }
	private static final RuleFactory printRuleFactory = RuleFactoryImpl.getInstance();
	private static final CSSParserFactory printParserFactory = CSSParserFactory.getInstance();
	
	// medium embossed
	private static final SupportedCSS brailleCSS = new SupportedBrailleCSS(false, true);
	private static DeclarationTransformer brailleDeclarationTransformer; static {
		// SupportedCSS injected via CSSFactory in DeclarationTransformer.<init>
		CSSFactory.registerSupportedCSS(brailleCSS);
		brailleDeclarationTransformer = new BrailleCSSDeclarationTransformer(); }
	private static final RuleFactory brailleRuleFactory = new BrailleCSSRuleFactory();
	private static final CSSParserFactory brailleParserFactory = new BrailleCSSParserFactory();
	
	private static class CssInlineTransformer implements DOMToXMLStreamTransformer {
		
		private final NetworkProcessor network;
		private final String defaultStyleSheet;
		private final String medium;
		private final javax.xml.namespace.QName attributeName;
		private final List<CascadedStyle> styles;
		
		public CssInlineTransformer(NetworkProcessor network,
		                            String defaultStyleSheet,
		                            String medium,
		                            javax.xml.namespace.QName attributeName) {
			this.network = network;
			this.defaultStyleSheet = defaultStyleSheet;
			this.medium = medium;
			this.attributeName = attributeName;
			this.styles = new ArrayList<CascadedStyle>();
		}
		
		private BaseURIAwareXMLStreamWriter writer;
		
		public void transform(Iterator<Document> input, Supplier<BaseURIAwareXMLStreamWriter> output) throws TransformerException {
			
			Document document = Iterators.getOnlyElement(input);
			this.writer = output.get();
			
			try {
				URI baseURI = new URI(document.getBaseURI());
				URL baseURL; {
					if (baseURI == null || baseURI.toString().equals(""))
						// handling the case where base URI is empty, although this should in theory never happen
						// FIXME: find out why it happens
						baseURL = null;
					else
						baseURL = asURL(baseURI);
				}
				URL[] defaultSheets; {
					StringTokenizer t = new StringTokenizer(defaultStyleSheet);
					ArrayList<URL> l = new ArrayList<URL>();
					while (t.hasMoreTokens())
						l.add(asURL(baseURI.resolve(asURI(t.nextToken()))));
					defaultSheets = toArray(l, URL.class);
				}
				
				CascadedStyle style = new CascadedStyle();
				styles.add(style);
				StyleSheet stylesheet;
				if (medium.equals("embossed")) {
					stylesheet = (StyleSheet)brailleRuleFactory.createStyleSheet().unlock();
					if (defaultSheets != null)
						for (URL sheet : defaultSheets)
							stylesheet = brailleParserFactory.append(sheet, network, null, SourceType.URL, stylesheet, sheet);
					// CSSParserFactory injected via CSSFactory in CSSAssignTraversal.<init>
					CSSFactory.registerCSSParserFactory(brailleParserFactory);
					stylesheet = CSSFactory.getUsedStyles(document, null, baseURL, new MediaSpec(medium), network, stylesheet);
					// DeclarationTransformer injected via CSSFactory in SingleMapNodeData.<init>
					// SupportedCSS injected via CSSFactory in SingleMapNodeData.<init>, Repeater.assignDefaults, Variator.assignDefaults
					CSSFactory.registerDeclarationTransformer(brailleDeclarationTransformer);
					CSSFactory.registerSupportedCSS(brailleCSS);
					style.styleMap = new Analyzer(stylesheet).evaluateDOM(document, medium, false);
					style.pageRules = new HashMap<String,Map<String,RulePage>>(); {
						for (RulePage r : filter(stylesheet, RulePage.class)) {
							String name = firstNonNull(r.getName(), "auto");
							String pseudo = firstNonNull(r.getPseudo(), "");
							Map<String,RulePage> pageRule = style.pageRules.get(name);
							if (pageRule == null) {
								pageRule = new HashMap<String,RulePage>();
								style.pageRules.put(name, pageRule); }
							if (pageRule.containsKey(pseudo))
								pageRule.put(pseudo, makePageRule(name, "".equals(pseudo) ? null : pseudo,
								                                  ImmutableList.of(r, pageRule.get(pseudo))));
							else
								pageRule.put(pseudo, r);
						}
					}
					style.volumeRules = new HashMap<String,Map<String,RuleVolume>>(); {
						for (RuleVolume r : filter(stylesheet, RuleVolume.class)) {
							String name = "auto";
							String pseudo = firstNonNull(r.getPseudo(), "");
							Map<String,RuleVolume> volumeRule = style.volumeRules.get(name);
							if (volumeRule == null) {
								volumeRule = new HashMap<String,RuleVolume>();
								style.volumeRules.put(name, volumeRule); }
							if (volumeRule.containsKey(pseudo))
								volumeRule.put(pseudo, makeVolumeRule(name, "".equals(pseudo) ? null : pseudo,
								                                      ImmutableList.of(r, volumeRule.get(pseudo))));
							else
								volumeRule.put(pseudo, r);
						}
					}
					style.textTransformRules = filter(stylesheet, RuleTextTransform.class);
					style.otherAtRules = filter(stylesheet, AnyAtRule.class);
				} else if (medium.equals("print")) {
					stylesheet = (StyleSheet)printRuleFactory.createStyleSheet().unlock();
					if (defaultSheets != null)
						for (URL sheet : defaultSheets) {
							// RuleFactory injected via CSSFactory in SimplePreparator.<init>, CSSTreeParser.<init>
							CSSFactory.registerRuleFactory(printRuleFactory);
							stylesheet = printParserFactory.append(sheet, network, null, SourceType.URL, stylesheet, sheet); }
					// CSSParserFactory injected via CSSFactory in CSSAssignTraversal.<init>
					// RuleFactory injected via CSSFactory in SimplePreparator.<init>, CSSTreeParser.<init>
					CSSFactory.registerCSSParserFactory(printParserFactory);
					CSSFactory.registerRuleFactory(printRuleFactory);
					stylesheet = CSSFactory.getUsedStyles(document, null, baseURL, new MediaSpec(medium), network, stylesheet);
					// DeclarationTransformer injected via CSSFactory in SingleMapNodeData.<init>
					// SupportedCSS injected via CSSFactory in SingleMapNodeData.<init>, Repeater.assignDefaults, Variator.assignDefaults
					CSSFactory.registerDeclarationTransformer(printDeclarationTransformer);
					CSSFactory.registerSupportedCSS(printCSS);
					style.styleMap = new Analyzer(stylesheet).evaluateDOM(document, medium, false);
				} else {
					throw new RuntimeException("medium " + medium + " not supported");
				}
				
				writer.setBaseURI(baseURI);
				writer.writeStartDocument();
				traverse(document.getDocumentElement());
				writer.writeEndDocument();
				
			} catch (Exception e) {
				throw new TransformerException(e);
			}
		}
		
		private static class CascadedStyle {
			StyleMap styleMap;
			Map<String,Map<String,RulePage>> pageRules;
			Map<String,Map<String,RuleVolume>> volumeRules;
			Iterable<RuleTextTransform> textTransformRules;
			Iterable<AnyAtRule> otherAtRules;
		}
		
		private boolean isRoot = true;
		
		private void traverse(Node node) throws XPathException, URISyntaxException, XMLStreamException {
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element)node;
				writeStartElement(writer, elem);
				NamedNodeMap attributes = node.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					Node attr = attributes.item(i);
					if (!(attr.getPrefix() == null && "style".equals(attr.getLocalName())))
						writeAttribute(writer, attr); }
				StringBuilder style = new StringBuilder();
				for (CascadedStyle cs : styles) {
					NodeData nodeData = cs.styleMap.get(elem);
					if (nodeData != null)
						insertStyle(style, nodeData);
					for (PseudoElement pseudo : sort(cs.styleMap.pseudoSet(elem), pseudoElementComparator)) {
						NodeData pseudoData = cs.styleMap.get(elem, pseudo);
						if (pseudoData != null)
							insertPseudoStyle(style, pseudoData, pseudo, cs.pageRules); }
					if (cs.pageRules != null) {
						Map<String,RulePage> pageRule = getPageRule(nodeData, cs.pageRules);
						if (pageRule != null) {
							insertPageStyle(style, pageRule, true); }
						else if (isRoot) {
							pageRule = getPageRule("auto", cs.pageRules);
							if (pageRule != null)
								insertPageStyle(style, pageRule, true); }}
					if (isRoot) {
						if (cs.volumeRules != null) {
							Map<String,RuleVolume> volumeRule = getVolumeRule("auto", cs.volumeRules);
							if (volumeRule != null)
								insertVolumeStyle(style, volumeRule, cs.pageRules); }
						if (cs.textTransformRules != null)
							for (RuleTextTransform r : cs.textTransformRules)
								insertTextTransformDefinition(style, r);
						if (cs.otherAtRules != null)
							for (AnyAtRule r : cs.otherAtRules) {
								if (style.length() > 0 && !style.toString().endsWith("} ")) {
									style.insert(0, "{ ");
									style.append("} "); }
								insertAtRule(style, r); }}}
				if (normalizeSpace(style).length() > 0) {
					writeAttribute(writer, attributeName, style.toString().trim()); }
				isRoot = false;
				for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
					traverse(child);
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
	
	@SuppressWarnings("unused")
	private static <T extends Comparable<? super T>> Iterable<T> sort(Iterable<T> iterable) {
		List<T> list = new ArrayList<T>();
		for (T x : iterable)
			list.add(x);
		Collections.<T>sort(list);
		return list;
	}
	
	private static <T> Iterable<T> sort(Iterable<T> iterable, Comparator<? super T> comparator) {
		List<T> list = new ArrayList<T>();
		for (T x : iterable)
			list.add(x);
		Collections.<T>sort(list, comparator);
		return list;
	}
	
	private static Comparator<PseudoElement> pseudoElementComparator = new Comparator<PseudoElement>() {
		public int compare(PseudoElement e1, PseudoElement e2) {
			return e1.toString().compareTo(e2.toString());
		}
	};
	
	// TODO: fix in braille-css and jstyleparser
	static Function<Term,String> termToString = new Function<Term,String>() {
		public String apply(Term term) {
			if (term instanceof TermInteger) {
				TermInteger integer = (TermInteger)term;
				return "" + integer.getIntValue(); }
			else if (term instanceof TermNumber) {
				TermNumber number = (TermNumber)term;
				Double value = number.getValue().doubleValue();
				if (value == Math.floor(value))
					return "" + value.intValue();
				else
					return "" + value; }
			else if (term instanceof TermPercent) {
				TermPercent percent = (TermPercent)term;
				Double value = percent.getValue().doubleValue();
				if (value == Math.floor(value))
					return "" + value.intValue() + "%";
				else
					return "" + value + "%"; }
			else if (term instanceof TermList && !(term instanceof TermFunction)) {
				TermList list = (TermList)term;
				String s = "";
				for (Term<?> t : list) {
					if (!s.isEmpty()) {
						Term.Operator o = t.getOperator();
						if (o != null)
							switch (o) {
							case COMMA:
								s += ","; }
						s += " "; }
					s += termToString.apply(t); }
				return s; }
			else if (term instanceof TermPair) {
				TermPair<?,?> pair = (TermPair<?,?>)term;
				Object val = pair.getValue();
				return "" + pair.getKey() + " " + (val instanceof Term ? termToString.apply((Term)val) : val.toString()); }
			else if (term instanceof TermString) {
				TermString string = (TermString)term;
				return "'" + string.getValue().replaceAll("\n", "\\\\A") + "'"; }
			else
				return term.toString().replaceAll("^[,/ ]+", "");
		}
	};
	
	private static void insertStyle(StringBuilder builder, NodeData nodeData) {
		List<String> keys = new ArrayList<String>(nodeData.getPropertyNames());
		keys.remove("page");
		Collections.sort(keys);
		for(String key : keys) {
			builder.append(key).append(": ");
			Term<?> value = nodeData.getValue(key, true);
			if (value != null)
				builder.append(termToString.apply(value));
			else {
				CSSProperty prop = nodeData.getProperty(key);
				builder.append(prop); }
			builder.append("; "); }
	}
	
	private static void pseudoElementToString(StringBuilder builder, PseudoElement elem) {
		if (elem instanceof PseudoElementImpl) {
			builder.append("&").append(elem);
			return; }
		else {
			builder.append("&::").append(elem.getName());
			String[] args = elem.getArguments();
			if (args.length > 0)
				builder.append("(").append(join(args, ", ")).append(")"); }
	}
	
	private static void insertPseudoStyle(StringBuilder builder, NodeData nodeData, PseudoElement elem,
	                                      Map<String,Map<String,RulePage>> pageRules) {
		pseudoElementToString(builder, elem);
		builder.append(" { ");
		insertStyle(builder, nodeData);
		Map<String,RulePage> pageRule = getPageRule(nodeData, pageRules);
		if (pageRule != null)
			insertPageStyle(builder, pageRule, false);
		builder.append("} ");
	}
	
	private static void insertPageStyle(StringBuilder builder, Map<String,RulePage> pageRule, boolean topLevel) {
		for (RulePage r : pageRule.values())
			insertPageStyle(builder, r, topLevel);
	}
	
	private static void insertPageStyle(StringBuilder builder, RulePage pageRule, boolean topLevel) {
		builder.append("@page");
		String pseudo = pageRule.getPseudo();
		if (pseudo != null && !"".equals(pseudo))
			builder.append(":").append(pseudo);
		builder.append(" { ");
		for (Declaration decl : filter(pageRule, Declaration.class))
			insertDeclaration(builder, decl);
		for (RuleMargin margin : filter(pageRule, RuleMargin.class))
			insertMarginStyle(builder, margin);
		builder.append("} ");
	}
	
	private static void insertMarginStyle(StringBuilder builder, RuleMargin ruleMargin) {
		builder.append("@").append(ruleMargin.getMarginArea()).append(" { ");
		insertStyle(builder, new SimpleInlineStyle(ruleMargin));
		builder.append("} ");
	}
	
	private static void insertDeclaration(StringBuilder builder, Declaration decl) {
		builder.append(decl.getProperty()).append(": ").append(join(decl, " ", termToString)).append("; ");
	}
	
	private static Map<String,RulePage> getPageRule(NodeData nodeData, Map<String,Map<String,RulePage>> pageRules) {
		BrailleCSSProperty.Page pageProperty; {
			if (nodeData != null)
				pageProperty = nodeData.<BrailleCSSProperty.Page>getProperty("page", false);
			else
				pageProperty = null;
		}
		String name; {
			if (pageProperty != null) {
				if (pageProperty == BrailleCSSProperty.Page.identifier)
					name = nodeData.<TermIdent>getValue(TermIdent.class, "page", false).getValue();
				else
					name = pageProperty.toString(); }
			else
				name = null;
		}
		if (name != null)
			return getPageRule(name, pageRules);
		else
			return null;
	}
	
	
	private static Map<String,RulePage> getPageRule(String name, Map<String,Map<String,RulePage>> pageRules) {
		Map<String,RulePage> auto = pageRules == null ? null : pageRules.get("auto");
		Map<String,RulePage> named = null;
		if (!name.equals("auto"))
			named = pageRules.get(name);
		Map<String,RulePage> result = new HashMap<String,RulePage>();
		List<RulePage> from;
		RulePage r;
		Set<String> pseudos = new HashSet<String>();
		if (named != null)
			pseudos.addAll(named.keySet());
		if (auto != null)
			pseudos.addAll(auto.keySet());
		for (String pseudo : pseudos) {
			boolean noPseudo = "".equals(pseudo);
			from = new ArrayList<RulePage>();
			if (named != null) {
				r = named.get(pseudo);
				if (r != null) from.add(r);
				if (!noPseudo) {
					r = named.get("");
					if (r != null) from.add(r); }}
			if (auto != null) {
				r = auto.get(pseudo);
				if (r != null) from.add(r);
				if (!noPseudo) {
					r = auto.get("");
					if (r != null) from.add(r); }}
			result.put(pseudo, makePageRule(name, noPseudo ? null : pseudo, from)); }
		return result;
	}
	
	private static RulePage makePageRule(String name, String pseudo, List<RulePage> from) {
		RulePage pageRule = brailleRuleFactory.createPage().setName(name).setPseudo(pseudo);
		for (RulePage f : from)
			for (Rule<?> r : f)
				if (r instanceof Declaration) {
					Declaration d = (Declaration)r;
					String property = d.getProperty();
					if (getDeclaration(pageRule, property) == null)
						pageRule.add(r); }
				else if (r instanceof RuleMargin) {
					RuleMargin m = (RuleMargin)r;
					String marginArea = m.getMarginArea();
					RuleMargin marginRule = getRuleMargin(pageRule, marginArea);
					if (marginRule == null) {
						marginRule = brailleRuleFactory.createMargin(marginArea);
						pageRule.add(marginRule);
						marginRule.replaceAll(m); }
					else
						for (Declaration d : m)
							if (getDeclaration(marginRule, d.getProperty()) == null)
								marginRule.add(d); }
		return pageRule;
	}
	
	private static Declaration getDeclaration(Collection<? extends Rule<?>> rule, String property) {
		for (Declaration d : filter(rule, Declaration.class))
			if (d.getProperty().equals(property))
				return d;
		return null;
	}
	
	private static RuleMargin getRuleMargin(Collection<? extends Rule<?>> rule, String marginArea) {
		for (RuleMargin m : filter(rule, RuleMargin.class))
			if (m.getMarginArea().equals(marginArea))
				return m;
		return null;
	}
	
	private static void insertVolumeStyle(StringBuilder builder, Map<String,RuleVolume> volumeRule, Map<String,Map<String,RulePage>> pageRules) {
		for (RuleVolume r : volumeRule.values())
			insertVolumeStyle(builder, r, pageRules);
	}
	
	private static void insertVolumeStyle(StringBuilder builder, RuleVolume volumeRule, Map<String,Map<String,RulePage>> pageRules) {
		builder.append("@volume");
		String pseudo = volumeRule.getPseudo();
		if (pseudo != null && !"".equals(pseudo))
			builder.append(":").append(pseudo);
		builder.append(" { ");
		for (Declaration decl : filter(volumeRule, Declaration.class))
			insertDeclaration(builder, decl);
		for (RuleVolumeArea volumeArea : filter(volumeRule, RuleVolumeArea.class))
			insertVolumeAreaStyle(builder, volumeArea, pageRules);
		builder.append("} ");
	}
	
	private static void insertVolumeAreaStyle(StringBuilder builder, RuleVolumeArea ruleVolumeArea, Map<String,Map<String,RulePage>> pageRules) {
		builder.append("@").append(ruleVolumeArea.getVolumeArea().value).append(" { ");
		StringBuilder innerStyle = new StringBuilder();
		Map<String,RulePage> pageRule = null;
		for (Declaration decl : filter(ruleVolumeArea, Declaration.class))
			if ("page".equals(decl.getProperty()))
				pageRule = getPageRule(join(decl, " ", termToString), pageRules);
			else
				insertDeclaration(innerStyle, decl);
		if (pageRule != null)
			insertPageStyle(innerStyle, pageRule, false);
		builder.append(innerStyle).append("} ");
	}
	
	private static void insertTextTransformDefinition(StringBuilder builder, RuleTextTransform rule) {
		builder.append("@text-transform ").append(rule.getName()).append(" { ");
		for (Declaration decl : rule)
			insertDeclaration(builder, decl);
		builder.append("} ");
	}
	
	private static void insertAtRule(StringBuilder builder, AnyAtRule rule) {
		builder.append("@").append(rule.getName()).append(" { ");
		for (Declaration decl : filter(rule, Declaration.class))
			insertDeclaration(builder, decl);
		for (AnyAtRule r : filter(rule, AnyAtRule.class))
			insertAtRule(builder, r);
		builder.append("} ");
	}
	
	// TODO: what about volumes that match both :first and :last?
	private static Map<String,RuleVolume> getVolumeRule(String name, Map<String,Map<String,RuleVolume>> volumeRules) {
		Map<String,RuleVolume> auto = volumeRules.get("auto");
		Map<String,RuleVolume> named = null;
		if (!name.equals("auto"))
			named = volumeRules.get(name);
		Map<String,RuleVolume> result = new HashMap<String,RuleVolume>();
		List<RuleVolume> from;
		RuleVolume r;
		Set<String> pseudos = new HashSet<String>();
		if (named != null)
			pseudos.addAll(named.keySet());
		if (auto != null)
			pseudos.addAll(auto.keySet());
		for (String pseudo : pseudos) {
			boolean noPseudo = "".equals(pseudo);
			from = new ArrayList<RuleVolume>();
			if (named != null) {
				r = named.get(pseudo);
				if (r != null) from.add(r);
				if (!noPseudo) {
					r = named.get("");
					if (r != null) from.add(r); }}
			if (auto != null) {
				r = auto.get(pseudo);
				if (r != null) from.add(r);
				if (!noPseudo) {
					r = auto.get("");
					if (r != null) from.add(r); }}
			result.put(pseudo, makeVolumeRule(name, noPseudo ? null : pseudo, from)); }
		return result;
	}
	
	private static final Pattern FUNCTION = Pattern.compile("(nth|nth-last)\\(([1-9][0-9]*)\\)");
	
	private static RuleVolume makeVolumeRule(String name, String pseudo, List<RuleVolume> from) {
		String arg = null;
		if (pseudo != null) {
			Matcher m = FUNCTION.matcher(pseudo);
			if (m.matches()) {
				pseudo = m.group(1);
				arg = m.group(2); }}
		RuleVolume volumeRule = new RuleVolume(pseudo, arg);
		for (RuleVolume f : from)
			for (Rule<?> r : f)
				if (r instanceof Declaration) {
					Declaration d = (Declaration)r;
					String property = d.getProperty();
					if (getDeclaration(volumeRule, property) == null)
						volumeRule.add(r); }
				else if (r instanceof RuleVolumeArea) {
					RuleVolumeArea a = (RuleVolumeArea)r;
					String volumeArea = a.getVolumeArea().value;
					RuleVolumeArea volumeAreaRule = getRuleVolumeArea(volumeRule, volumeArea);
					if (volumeAreaRule == null) {
						volumeAreaRule = new RuleVolumeArea(volumeArea);
						volumeRule.add(volumeAreaRule);
						volumeAreaRule.replaceAll(a); }
					else
						for (Declaration d : filter(a, Declaration.class))
							if (getDeclaration(volumeAreaRule, d.getProperty()) == null)
								volumeAreaRule.add(d); }
		return volumeRule;
	}
	
	private static RuleVolumeArea getRuleVolumeArea(Collection<? extends Rule<?>> rule, String volumeArea) {
		for (RuleVolumeArea m : filter(rule, RuleVolumeArea.class))
			if (m.getVolumeArea().value.equals(volumeArea))
				return m;
		return null;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CssInlineStep.class);
	
}
