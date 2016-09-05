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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import static com.google.common.base.Objects.firstNonNull;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterators.addAll;
import com.google.common.io.ByteSource;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

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
import cz.vutbr.web.css.TermPair;
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

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.NamespaceIterator;

import org.apache.commons.io.input.BOMInputStream;

import org.daisy.braille.css.BrailleCSSDeclarationTransformer;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSProperty;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.SupportedBrailleCSS;
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

public class CSSInlineStep extends DefaultStep {
	
	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;
	private Map<String,String> sassVariables = new HashMap<String,String>();
	private NetworkProcessor network = null;
	private Importer importer = null;
	
	private final String scssNumber = "\\d*\\.\\d+";
	private final String scssColor = "(#[\\da-zA-Z]+|(rgb|hsl)a?\\([^)]*\\))";
	private final String scssBadStringChars = "!\"#$'()*+,\\.\\/:<=>?@\\[\\\\\\]^`{|}~-";
	private final String scssNumberColorString = "\\s*(\\s*|"+ scssNumber +"|"+ scssColor +"|"+ "[^"+scssBadStringChars+"]+" +"|"+ "\\\"[^'"+scssBadStringChars+"]+\\\"" +"|"+ "'[^\\\""+scssBadStringChars+"]+'" +")\\s*";
	private final String scssValue = scssNumberColorString + "(" + "(\\s+|\\s*,\\s*)" + scssNumberColorString + ")*";
	
	private static final QName _default_stylesheet = new QName("default-stylesheet");
	private static final QName _media = new QName("media");
	private static final QName _attribute_name = new QName("attribute-name");
	
	private static final String DEFAULT_MEDIA = "embossed";
	private static final QName DEFAULT_ATTRIBUTE_NAME = new QName("style");
	
	private CSSInlineStep(XProcRuntime runtime, XAtomicStep step, final URIResolver resolver) {
		super(runtime, step);
		importer = new Importer() {
			public Collection<Import> apply(String url, Import previous) {
				try {
					URI uri = asURI(url);
					URI base = previous.getAbsoluteUri();
					logger.debug("Importing SASS style sheet: " + uri + " (base = " + base + ")");
					URI abs = base.resolve(uri);
					try {
						Source resolved = resolver.resolve(abs.toString(), "");
						if (resolved != null) {
							abs = asURI(resolved.getSystemId());
							logger.debug("Resolved to: " + abs); }}
					catch (TransformerException e) {
						throw new IOException(e); }
					try {
						return ImmutableList.of(
							new Import(uri, abs,
							           byteSource(asURL(abs).openStream()).asCharSource(StandardCharsets.UTF_8).read())); }
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
				try {
					Source resolved = resolver.resolve(asURI(url).toString(), "");
					if (resolved != null) {
						url = new URL(resolved.getSystemId());
						logger.debug("Resolved to :" + url); }}
				catch (TransformerException e) {
					throw new IOException(e); }
				InputStream is = super.fetch(url);
				
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
						if (value.matches("\\s*")) {
							logger.debug("scss variable '"+var+"' contains only white space: "+value);
							value = "'"+value+"'";
							logger.debug("scss variable '"+var+"' was quoted               : "+value);
						} else if (!value.matches(scssValue)) {
							// if value contains special characters that can mess up parsing; wrap it in single quotes
							logger.debug("scss variable '"+var+"' contains special characters: "+value);
							value = "'"+value.replaceAll("'", "\\\\'")+"'";
							logger.debug("scss variable '"+var+"' was escaped                : "+value);
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
		sourcePipe = pipe;
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
			XdmNode source = sourcePipe.read();
			Document doc = (Document)DocumentOverNodeInfo.wrap(source.getUnderlyingNode());
			URI base = asURI(doc.getBaseURI());
			URL[] defaultSheets; {
				StringTokenizer t = new StringTokenizer(getOption(_default_stylesheet, ""));
				ArrayList<URL> l = new ArrayList<URL>();
				while (t.hasMoreTokens())
					l.add(asURL(base.resolve(asURI(t.nextToken()))));
				defaultSheets = toArray(l, URL.class);
			}
			Set<String> media = ImmutableSet.copyOf(Splitter.on(' ').omitEmptyStrings().split(getOption(_media, DEFAULT_MEDIA)));
			QName attributeName = getOption(_attribute_name, DEFAULT_ATTRIBUTE_NAME);
			resultPipe.write((new InlineCSSWriter(doc, runtime, network, defaultSheets, media, attributeName)).getResult()); }
		catch (Exception e) {
			logger.error("css:inline failed", e);
			throw new XProcException(step.getNode(), e); }
	}
	
	@Component(
		name = "css:inline",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/braille-css}inline" }
	)
	public static class Provider implements XProcStepProvider {
		
		private URIResolver resolver;
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new CSSInlineStep(runtime, step, resolver);
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
	
	// media print
	private static final SupportedCSS printCSS = SupportedPrintCSS.getInstance();
	private static DeclarationTransformer printDeclarationTransformer; static {
		// SupportedCSS injected via CSSFactory in DeclarationTransformer.<init>
		CSSFactory.registerSupportedCSS(printCSS);
		printDeclarationTransformer = new DeclarationTransformer() {}; }
	private static final RuleFactory printRuleFactory = RuleFactoryImpl.getInstance();
	private static final CSSParserFactory printParserFactory = CSSParserFactory.getInstance();
	
	// media embossed
	private static final SupportedCSS brailleCSS = new SupportedBrailleCSS(false, true);
	private static DeclarationTransformer brailleDeclarationTransformer; static {
		// SupportedCSS injected via CSSFactory in DeclarationTransformer.<init>
		CSSFactory.registerSupportedCSS(brailleCSS);
		brailleDeclarationTransformer = new BrailleCSSDeclarationTransformer(); }
	private static final RuleFactory brailleRuleFactory = new BrailleCSSRuleFactory();
	private static final CSSParserFactory brailleParserFactory = new BrailleCSSParserFactory();
	
	
	private static class InlineCSSWriter extends TreeWriter {
		
		private final List<CascadedStyle> styles;
		private final QName attributeName;
		
		public InlineCSSWriter(Document document,
		                       XProcRuntime xproc,
		                       NetworkProcessor network,
		                       URL[] defaultSheets,
		                       Set<String> media,
		                       QName attributeName) throws Exception {
			super(xproc);
			this.styles = new ArrayList<CascadedStyle>();
			this.attributeName = attributeName;
			
			URI baseURI = new URI(document.getBaseURI());
			
			for (String medium : media) {
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
					stylesheet = CSSFactory.getUsedStyles(document, null, asURL(baseURI), new MediaSpec(medium), network, stylesheet);
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
					stylesheet = CSSFactory.getUsedStyles(document, null, asURL(baseURI), new MediaSpec(medium), network, stylesheet);
					// DeclarationTransformer injected via CSSFactory in SingleMapNodeData.<init>
					// SupportedCSS injected via CSSFactory in SingleMapNodeData.<init>, Repeater.assignDefaults, Variator.assignDefaults
					CSSFactory.registerDeclarationTransformer(printDeclarationTransformer);
					CSSFactory.registerSupportedCSS(printCSS);
					style.styleMap = new Analyzer(stylesheet).evaluateDOM(document, medium, false);
				} else {
					throw new RuntimeException("medium " + medium + " not supported");
				}
			}
			
			startDocument(baseURI);
			traverse(document.getDocumentElement());
			endDocument();
		}
		
		private static class CascadedStyle {
			StyleMap styleMap;
			Map<String,Map<String,RulePage>> pageRules;
			Map<String,Map<String,RuleVolume>> volumeRules;
			Iterable<RuleTextTransform> textTransformRules;
		}
		
		private void traverse(Node node) throws XPathException, URISyntaxException {
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				boolean isRoot = !seenRoot;
				Element elem = (Element)node;
				addStartElement(elem);
				NamedNodeMap attributes = node.getAttributes();
				for (int i=0; i<attributes.getLength(); i++) {
					Node attr = attributes.item(i);
					if ("http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())) {}
					else if (attr.getPrefix() != null)
						addAttribute(new QName(attr.getPrefix(), attr.getNamespaceURI(), attr.getLocalName()), attr.getNodeValue());
					else if ("style".equals(attr.getLocalName())) {}
					else
						addAttribute(new QName(attr.getNamespaceURI(), attr.getLocalName()), attr.getNodeValue()); }
				StringBuilder style = new StringBuilder();
				for (CascadedStyle cs : styles) {
					NodeData nodeData = cs.styleMap.get(elem);
					if (nodeData != null)
						insertStyle(style, nodeData);
					for (PseudoElement pseudo : sort(cs.styleMap.pseudoSet(elem), pseudoElementComparator)) {
						NodeData pseudoData = cs.styleMap.get(elem, pseudo);
						if (pseudoData != null)
							insertPseudoStyle(style, pseudoData, pseudo); }
					if (cs.pageRules != null) {
						BrailleCSSProperty.Page pageProperty = null;
						if (nodeData != null)
							pageProperty = nodeData.<BrailleCSSProperty.Page>getProperty("page", false);
						if (pageProperty != null) {
							String name;
							if (pageProperty == BrailleCSSProperty.Page.identifier)
								name = nodeData.<TermIdent>getValue(TermIdent.class, "page", false).getValue();
							else
								name = pageProperty.toString();
							Map<String,RulePage> pageRule = getPageRule(name, cs.pageRules);
							if (pageRule != null)
								insertPageStyle(style, pageRule, true); }
						else if (isRoot) {
							Map<String,RulePage> pageRule = getPageRule("auto", cs.pageRules);
							if (pageRule != null)
								insertPageStyle(style, pageRule, true); }
						if (isRoot) {
							if (cs.volumeRules != null) {
								Map<String,RuleVolume> volumeRule = getVolumeRule("auto", cs.volumeRules);
								if (volumeRule != null)
									insertVolumeStyle(style, volumeRule, cs.pageRules); }
							if (cs.textTransformRules != null)
								for (RuleTextTransform r : cs.textTransformRules)
									insertTextTransformDefinition(style, r); }}}
				if (normalizeSpace(style).length() > 0) {
					addAttribute(attributeName, style.toString().trim()); }
				receiver.startContent();
				for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
					traverse(child);
				addEndElement(); }
			else if (node.getNodeType() == Node.COMMENT_NODE)
				addComment(node.getNodeValue());
			else if (node.getNodeType() == Node.TEXT_NODE)
				addText(node.getNodeValue());
			else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
				addPI(node.getLocalName(), node.getNodeValue());
			else
				throw new UnsupportedOperationException("Unexpected node type");
		}
		
		public void addStartElement(Element element) {
			NodeInfo inode = ((NodeOverNodeInfo)element).getUnderlyingNodeInfo();
			NamespaceBinding[] inscopeNS = null;
			if (seenRoot)
				inscopeNS = inode.getDeclaredNamespaces(null);
			else {
				List<NamespaceBinding> namespaces = new ArrayList<NamespaceBinding>();
				addAll(namespaces, NamespaceIterator.iterateNamespaces(inode));
				inscopeNS = toArray(namespaces, NamespaceBinding.class);
				seenRoot = true; }
			receiver.setSystemId(element.getBaseURI());
			addStartElement(new NameOfNode(inode), inode.getSchemaType(), inscopeNS);
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
	
	private static Function<Object,String> termToString = new Function<Object,String>() {
		public String apply(Object term) {
			if (term instanceof TermInteger) {
				TermInteger integer = (TermInteger)term;
				return "" + integer.getIntValue(); }
			else if (term instanceof TermPair) {
				TermPair<?,?> pair = (TermPair<?,?>)term;
				Term.Operator op = pair.getOperator();
				return (op != null ? op.value() : "") + pair.getKey() + " " + pair.getValue(); }
			else if (term instanceof TermFunction)
				return "" + term;
			else if (term instanceof TermList) {
				TermList list = (TermList)term;
				return join(list, " ", termToString); }
			else
				return "" + term;
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
			builder.append(elem);
			return; }
		else {
			builder.append("::").append(elem.getName());
			String[] args = elem.getArguments();
			if (args.length > 0)
				builder.append("(").append(join(args, ", ")).append(")"); }
	}
	
	private static void insertPseudoStyle(StringBuilder builder, NodeData nodeData, PseudoElement elem) {
		if (builder.length() > 0 && !builder.toString().endsWith("} ")) {
			builder.insert(0, "{ ");
			builder.append("} "); }
		pseudoElementToString(builder, elem);
		builder.append(" { ");
		insertStyle(builder, nodeData);
		builder.append("} ");
	}
	
	private static void insertPageStyle(StringBuilder builder, Map<String,RulePage> pageRule, boolean topLevel) {
		for (RulePage r : pageRule.values())
			insertPageStyle(builder, r, topLevel);
	}
	
	private static void insertPageStyle(StringBuilder builder, RulePage pageRule, boolean topLevel) {
		if (topLevel && builder.length() > 0 && !builder.toString().endsWith("} ")) {
			builder.insert(0, "{ ");
			builder.append("} "); }
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
		for (Declaration decl : ruleMargin)
			insertDeclaration(builder, decl);
		builder.append("} ");
	}
	
	private static void insertDeclaration(StringBuilder builder, Declaration decl) {
		builder.append(decl.getProperty()).append(": ").append(join(decl, " ", termToString)).append("; ");
	}
	
	private static Map<String,RulePage> getPageRule(String name, Map<String,Map<String,RulePage>> pageRules) {
		Map<String,RulePage> auto = pageRules.get("auto");
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
		if (builder.length() > 0 && !builder.toString().endsWith("} ")) {
			builder.insert(0, "{ ");
			builder.append("} "); }
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
		for (Declaration decl : ruleVolumeArea)
			if ("page".equals(decl.getProperty()))
				pageRule = getPageRule(join(decl, " ", termToString), pageRules);
			else
				insertDeclaration(innerStyle, decl);
		if (pageRule != null)
			insertPageStyle(innerStyle, pageRule, false);
		builder.append(innerStyle).append("} ");
	}
	
	private static void insertTextTransformDefinition(StringBuilder builder, RuleTextTransform rule) {
		if (builder.length() > 0 && !builder.toString().endsWith("} ")) {
			builder.insert(0, "{ ");
			builder.append("} "); }
		builder.append("@text-transform ").append(rule.getName()).append(" { ");
		for (Declaration decl : rule)
			insertDeclaration(builder, decl);
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
						for (Declaration d : a)
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
	
	private static final Logger logger = LoggerFactory.getLogger(CSSInlineStep.class);
	
}
