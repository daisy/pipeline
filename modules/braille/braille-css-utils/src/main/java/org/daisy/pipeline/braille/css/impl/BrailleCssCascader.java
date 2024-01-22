package org.daisy.pipeline.braille.css.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.URIResolver;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.RuleFactoryImpl;
import cz.vutbr.web.domassign.DeclarationTransformer;

import org.daisy.braille.css.AnyAtRule;
import org.daisy.braille.css.BrailleCSSDeclarationTransformer;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSProperty;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.RuleCounterStyle;
import org.daisy.braille.css.RuleHyphenationResource;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.file.URLs;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.pipeline.braille.css.SupportedPrintCSS;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.css.CssCascader;
import org.daisy.pipeline.css.CssPreProcessor;
import org.daisy.pipeline.css.JStyleParserCssCascader;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.XsltProcessor;

import org.osgi.service.component.annotations.Component;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Component(
	name = "BrailleCssCascader",
	service = { CssCascader.class }
)
public class BrailleCssCascader implements CssCascader {

	/**
	 * Note that this implementation only supports a very small subset of medium "print", namely the
	 * properties color, font-style, font-weight, text-decoration.
	 */
	public boolean supportsMedium(Medium medium) {
		switch (medium.getType()) {
		case EMBOSSED:
		case PRINT:
			return true;
		default:
			return false;
		}
	}

	public XMLTransformer newInstance(Medium medium,
	                                  String userStylesheet,
	                                  URIResolver uriResolver,
	                                  CssPreProcessor preProcessor,
	                                  XsltProcessor xsltProcessor,
	                                  QName attributeName,
	                                  boolean multipleAttrs) {
		if (multipleAttrs)
			throw new UnsupportedOperationException("Cascading to multiple attributes per element not supported");
		if (attributeName == null)
			throw new UnsupportedOperationException("A style attribute must be specified");
		switch (medium.getType()) {
		case EMBOSSED:
			return new Transformer(uriResolver, preProcessor, xsltProcessor, userStylesheet, medium, attributeName,
			                       brailleParserFactory, brailleRuleFactory, brailleCSS, brailleDeclarationTransformer);
		case PRINT:
			return new Transformer(uriResolver, preProcessor, xsltProcessor, userStylesheet, medium, attributeName,
			                       printParserFactory, printRuleFactory, printCSS, printDeclarationTransformer);
		default:
			throw new IllegalArgumentException("medium not supported: " + medium);
		}
	}

	// medium print
	private static final SupportedCSS printCSS = SupportedPrintCSS.getInstance();
	private static DeclarationTransformer printDeclarationTransformer = new DeclarationTransformer(printCSS);
	private static final RuleFactory printRuleFactory = RuleFactoryImpl.getInstance();
	private static final CSSParserFactory printParserFactory = CSSParserFactory.getInstance();

	// medium embossed
	private static final SupportedCSS brailleCSS = new SupportedBrailleCSS(false, true);
	private static final DeclarationTransformer brailleDeclarationTransformer
		= new BrailleCSSDeclarationTransformer(brailleCSS);
	private static final RuleFactory brailleRuleFactory = new BrailleCSSRuleFactory();
	private static final CSSParserFactory brailleParserFactory = new BrailleCSSParserFactory();

	private static class Transformer extends JStyleParserCssCascader {

		private final QName attributeName;
		private final boolean isBrailleCss;

		private Transformer(URIResolver resolver, CssPreProcessor preProcessor, XsltProcessor xsltProcessor,
		                    String userStyleSheet, Medium medium, QName attributeName,
		                    CSSParserFactory parserFactory, RuleFactory ruleFactory,
		                    SupportedCSS supportedCss, DeclarationTransformer declarationTransformer) {
			super(resolver, preProcessor, xsltProcessor, userStyleSheet, medium, attributeName,
			      parserFactory, ruleFactory, supportedCss, declarationTransformer);
			this.attributeName = attributeName;
			this.isBrailleCss = medium.getType() == Medium.Type.EMBOSSED;
		}

		private Map<String,Map<String,RulePage>> pageRules = null;
		private Map<String,Map<String,RuleVolume>> volumeRules = null;
		private Iterable<RuleTextTransform> textTransformRules = null;
		private Iterable<RuleHyphenationResource> hyphenationResourceRules = null;
		private Iterable<RuleCounterStyle> counterStyleRules = null;
		private Iterable<AnyAtRule> otherAtRules = null;

		protected Map<QName,String> serializeStyle(NodeData mainStyle, Map<PseudoElement,NodeData> pseudoStyles, Element context) {
			if (isBrailleCss && pageRules == null) {
				StyleSheet styleSheet = getParsedStyleSheet();
				pageRules = new HashMap<String,Map<String,RulePage>>(); {
					for (RulePage r : Iterables.filter(styleSheet, RulePage.class)) {
						String name = MoreObjects.firstNonNull(r.getName(), "auto");
						String pseudo = MoreObjects.firstNonNull(r.getPseudo(), "");
						Map<String,RulePage> pageRule = pageRules.get(name);
						if (pageRule == null) {
							pageRule = new HashMap<String,RulePage>();
							pageRules.put(name, pageRule); }
						if (pageRule.containsKey(pseudo))
							pageRule.put(pseudo, makePageRule(name, "".equals(pseudo) ? null : pseudo,
							                                  ImmutableList.of(r, pageRule.get(pseudo))));
						else
							pageRule.put(pseudo, r);
					}
				}
				volumeRules = new HashMap<String,Map<String,RuleVolume>>(); {
					for (RuleVolume r : Iterables.filter(styleSheet, RuleVolume.class)) {
						String name = "auto";
						String pseudo = MoreObjects.firstNonNull(r.getPseudo(), "");
						if (pseudo.equals("nth(1)")) pseudo = "first";
						else if (pseudo.equals("nth-last(1)")) pseudo = "last";
						Map<String,RuleVolume> volumeRule = volumeRules.get(name);
						if (volumeRule == null) {
							volumeRule = new HashMap<String,RuleVolume>();
							volumeRules.put(name, volumeRule); }
						if (volumeRule.containsKey(pseudo))
							volumeRule.put(pseudo, makeVolumeRule("".equals(pseudo) ? null : pseudo,
							                                      ImmutableList.of(r, volumeRule.get(pseudo))));
						else
							volumeRule.put(pseudo, r);
					}
				}
				textTransformRules = Iterables.filter(styleSheet, RuleTextTransform.class);
				hyphenationResourceRules = Iterables.filter(styleSheet, RuleHyphenationResource.class);
				counterStyleRules = Iterables.filter(styleSheet, RuleCounterStyle.class);
				otherAtRules = Iterables.filter(styleSheet, AnyAtRule.class);
			}
			StringBuilder style = new StringBuilder();
			if (mainStyle != null)
				insertStyle(style, mainStyle);
			for (PseudoElement pseudo : sort(pseudoStyles.keySet(), pseudoElementComparator)) {
				NodeData nd = pseudoStyles.get(pseudo);
				if (nd != null)
					insertPseudoStyle(style, nd, pseudo, pageRules);
			}
			if (isBrailleCss) {
				boolean isRoot = (context.getParentNode().getNodeType() != Node.ELEMENT_NODE);
				Map<String,RulePage> pageRule = getPageRule(mainStyle, pageRules);
				if (pageRule != null) {
					insertPageStyle(style, pageRule, true); }
				else if (isRoot) {
					pageRule = getPageRule("auto", pageRules);
					if (pageRule != null)
						insertPageStyle(style, pageRule, true); }
				if (isRoot) {
					Map<String,RuleVolume> volumeRule = getVolumeRule("auto", volumeRules);
					if (volumeRule != null)
						insertVolumeStyle(style, volumeRule, pageRules);
					for (RuleTextTransform r : textTransformRules)
						insertTextTransformDefinition(style, r);
					for (RuleHyphenationResource r : hyphenationResourceRules)
						insertHyphenationResourceDefinition(style, r);
					for (RuleCounterStyle r : counterStyleRules)
						insertCounterStyleDefinition(style, r);
					for (AnyAtRule r : otherAtRules) {
						if (style.length() > 0 && !style.toString().endsWith("} ")) {
							style.insert(0, "{ ");
							style.append("} "); }
						insertAtRule(style, r); }}}
			if (!style.toString().replaceAll("\\s+", "").isEmpty())
				return ImmutableMap.of(attributeName, style.toString().trim());
			else
				return null;
		}

		protected String serializeValue(Term<?> value) {
			return BrailleCssSerializer.toString(value);
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

	// FIXME: move parts of this to BrailleCssSerializer

	private static void insertStyle(StringBuilder builder, NodeData nodeData) {
		List<String> keys = new ArrayList<String>(nodeData.getPropertyNames());
		keys.remove("page");
		Collections.sort(keys);
		for (String key : keys) {
			Term<?> value = nodeData.getValue(key, false);
			if (value != null)
				builder.append(key).append(": ").append(BrailleCssSerializer.toString(value)).append("; ");
			else {
				CSSProperty prop = nodeData.getProperty(key, false);
				if (prop != null) // can be null for unspecified inherited properties
					builder.append(key).append(": ").append(prop).append("; "); }}
	}

	private static void pseudoElementToString(StringBuilder builder, PseudoElement elem) {
		if (elem instanceof PseudoElementImpl) {
			builder.append("&").append(elem);
			return; }
		else {
			builder.append("&::").append(elem.getName());
			String[] args = elem.getArguments();
			if (args.length > 0) {
				StringBuilder s = new StringBuilder();
				Iterator<String> it = Arrays.asList(args).iterator();
				while (it.hasNext()) {
					s.append(it.next());
					if (it.hasNext()) s.append(", "); }
				builder.append("(").append(s).append(")"); }}
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
		for (Declaration decl : Iterables.filter(pageRule, Declaration.class))
			insertDeclaration(builder, decl);
		for (RuleMargin margin : Iterables.filter(pageRule, RuleMargin.class))
			insertMarginStyle(builder, margin);
		builder.append("} ");
	}

	private static void insertMarginStyle(StringBuilder builder, RuleMargin ruleMargin) {
		builder.append("@").append(ruleMargin.getMarginArea()).append(" { ");
		insertStyle(builder, new SimpleInlineStyle(ruleMargin, null, brailleDeclarationTransformer, brailleCSS));
		builder.append("} ");
	}

	private static void insertDeclaration(StringBuilder builder, Declaration decl) {
		StringBuilder s = new StringBuilder();
		Iterator<Term<?>> it = decl.iterator();
		while (it.hasNext()) {
			s.append(BrailleCssSerializer.toString(it.next()));
			if (it.hasNext()) s.append(" "); }
		builder.append(decl.getProperty()).append(": ").append(s).append("; ");
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
		for (Declaration d : Iterables.filter(rule, Declaration.class))
			if (d.getProperty().equals(property))
				return d;
		return null;
	}

	private static RuleMargin getRuleMargin(Collection<? extends Rule<?>> rule, String marginArea) {
		for (RuleMargin m : Iterables.filter(rule, RuleMargin.class))
			if (m.getMarginArea().equals(marginArea))
				return m;
		return null;
	}

	private static void insertVolumeStyle(StringBuilder builder, Map<String,RuleVolume> volumeRule, Map<String,Map<String,RulePage>> pageRules) {
		for (Map.Entry<String,RuleVolume> r : volumeRule.entrySet())
			insertVolumeStyle(builder, r, pageRules);
	}

	private static void insertVolumeStyle(StringBuilder builder, Map.Entry<String,RuleVolume> volumeRule, Map<String,Map<String,RulePage>> pageRules) {
		builder.append("@volume");
		String pseudo = volumeRule.getKey();
		if (pseudo != null && !"".equals(pseudo))
			builder.append(":").append(pseudo);
		builder.append(" { ");
		for (Declaration decl : Iterables.filter(volumeRule.getValue(), Declaration.class))
			insertDeclaration(builder, decl);
		for (RuleVolumeArea volumeArea : Iterables.filter(volumeRule.getValue(), RuleVolumeArea.class))
			insertVolumeAreaStyle(builder, volumeArea, pageRules);
		builder.append("} ");
	}

	private static void insertVolumeAreaStyle(StringBuilder builder, RuleVolumeArea ruleVolumeArea, Map<String,Map<String,RulePage>> pageRules) {
		builder.append("@").append(ruleVolumeArea.getVolumeArea().value).append(" { ");
		StringBuilder innerStyle = new StringBuilder();
		Map<String,RulePage> pageRule = null;
		for (Declaration decl : Iterables.filter(ruleVolumeArea, Declaration.class))
			if ("page".equals(decl.getProperty())) {
				StringBuilder s = new StringBuilder();
				Iterator<Term<?>> it = decl.iterator();
				while (it.hasNext()) {
					s.append(BrailleCssSerializer.toString(it.next()));
					if (it.hasNext()) s.append(" "); }
				pageRule = getPageRule(s.toString(), pageRules); }
			else
				insertDeclaration(innerStyle, decl);
		if (pageRule != null)
			insertPageStyle(innerStyle, pageRule, false);
		builder.append(innerStyle).append("} ");
	}

	private static void insertTextTransformDefinition(StringBuilder builder, RuleTextTransform rule) {
		builder.append("@text-transform");
		String name = rule.getName();
		if (name != null) builder.append(' ').append(name);
		builder.append(" { ");
		for (Declaration decl : rule) {
			if (decl.size() == 1 && decl.get(0) instanceof TermURI) {
				TermURI term = (TermURI)decl.get(0);
				URI uri = URLs.asURI(term.getValue());
				if (!uri.isAbsolute() && !uri.getSchemeSpecificPart().startsWith("/")) {
					// relative resource: make absolute and convert to "volatile-file" URI to bypass
					// caching in AbstractTransformProvider
					if (term.getBase() != null)
						uri = URLs.resolve(URLs.asURI(term.getBase()), uri);
					try {
						new File(uri);
						try {
							uri = new URI("volatile-file", uri.getSchemeSpecificPart(), uri.getFragment());
						} catch (URISyntaxException e) {
							throw new IllegalStateException(e); // should not happen
						}
					} catch (IllegalArgumentException e) {
						// not a file URI
					}
				}
				builder.append(decl.getProperty()).append(": ").append("url(\"" + uri + "\")").append("; ");
				continue;
			}
			insertDeclaration(builder, decl);
		}
		builder.append("} ");
	}

	private static void insertHyphenationResourceDefinition(StringBuilder builder, RuleHyphenationResource rule) {
		builder.append("@hyphenation-resource");
		builder.append(":lang(").append(BrailleCssSerializer.serializeLanguageRanges(rule.getLanguageRanges())).append(")");
		builder.append(" { ");
		for (Declaration decl : rule) {
			if (decl.size() == 1 && decl.get(0) instanceof TermURI) {
				TermURI term = (TermURI)decl.get(0);
				URI uri = URLs.asURI(term.getValue());
				if (!uri.isAbsolute() && !uri.getSchemeSpecificPart().startsWith("/")) {
					// relative resource: make absolute and convert to "volatile-file" URI to bypass
					// caching in AbstractTransformProvider
					if (term.getBase() != null)
						uri = URLs.resolve(URLs.asURI(term.getBase()), uri);
					try {
						new File(uri);
						try {
							uri = new URI("volatile-file", uri.getSchemeSpecificPart(), uri.getFragment());
						} catch (URISyntaxException e) {
							throw new IllegalStateException(e); // should not happen
						}
					} catch (IllegalArgumentException e) {
						// not a file URI
					}
				}
				builder.append(decl.getProperty()).append(": ").append("url(\"" + uri + "\")").append("; ");
				continue;
			}
			insertDeclaration(builder, decl);
		}
		builder.append("} ");
	}

	private static void insertCounterStyleDefinition(StringBuilder builder, RuleCounterStyle rule) {
		String name = rule.getName();
		builder.append("@counter-style ").append(name).append(" { ");
		for (Declaration decl : rule)
			insertDeclaration(builder, decl);
		builder.append("} ");
	}

	private static void insertAtRule(StringBuilder builder, AnyAtRule rule) {
		builder.append("@").append(rule.getName()).append(" { ");
		for (Declaration decl : Iterables.filter(rule, Declaration.class))
			insertDeclaration(builder, decl);
		for (AnyAtRule r : Iterables.filter(rule, AnyAtRule.class))
			insertAtRule(builder, r);
		builder.append("} ");
	}

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
		// Create a special rule for volumes that match both :first and :last (i.e. for the case
		// there is only a single volume)
		// FIXME: The order in which rules are defined currently does not affect the
		// precedence. ':first' rules always override ':last' rules.
		if (pseudos.contains("first") && pseudos.contains("last"))
			pseudos.add("only");
		for (String pseudo : pseudos) {
			from = new ArrayList<RuleVolume>();
			if (named != null) {
				r = named.get(pseudo);
				if (r != null) from.add(r);
				if ("only".equals(pseudo)) {
					r = named.get("first");
					if (r != null) from.add(r);
					r = named.get("last");
					if (r != null) from.add(r); }
				if (!"".equals(pseudo)) {
					r = named.get("");
					if (r != null) from.add(r); }}
			if (auto != null) {
				r = auto.get(pseudo);
				if (r != null) from.add(r);
				if ("only".equals(pseudo)) {
					r = auto.get("first");
					if (r != null) from.add(r);
					r = auto.get("last");
					if (r != null) from.add(r); }
				if (!"".equals(pseudo)) {
					r = auto.get("");
					if (r != null) from.add(r); }}
			result.put(pseudo,
			           makeVolumeRule(
			               // "only" is not a valid pseudo name so we drop it. The value we pass to
			               // makeVolumeRule() does not matter anyway as long as we use the
			               // corresponding key of the map where we store the volume rule to
			               // serialize the rule.
			               ("".equals(pseudo) || "only".equals(pseudo)) ? null : pseudo,
			               from)); }
		return result;
	}

	private static final Pattern FUNCTION = Pattern.compile("(nth|nth-last)\\(([1-9][0-9]*)\\)");

	private static RuleVolume makeVolumeRule(String pseudo, List<RuleVolume> from) {
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
						for (Declaration d : Iterables.filter(a, Declaration.class))
							if (getDeclaration(volumeAreaRule, d.getProperty()) == null)
								volumeAreaRule.add(d); }
		return volumeRule;
	}

	private static RuleVolumeArea getRuleVolumeArea(Collection<? extends Rule<?>> rule, String volumeArea) {
		for (RuleVolumeArea m : Iterables.filter(rule, RuleVolumeArea.class))
			if (m.getVolumeArea().value.equals(volumeArea))
				return m;
		return null;
	}
}
