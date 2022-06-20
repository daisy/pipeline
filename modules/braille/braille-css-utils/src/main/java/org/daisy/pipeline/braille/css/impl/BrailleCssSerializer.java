package org.daisy.pipeline.braille.css.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.CounterIncrement;
import cz.vutbr.web.css.CSSProperty.CounterSet;
import cz.vutbr.web.css.CSSProperty.CounterReset;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.csskit.OutputUtil;

import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.css.impl.ContentList.AttrFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.ContentFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.CounterFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.CounterStyle;
import org.daisy.pipeline.braille.css.impl.ContentList.FlowFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.LeaderFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.StringFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.TextFunction;
import org.daisy.pipeline.braille.css.impl.ContentList.URL;
import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.LanguageRange;
import org.daisy.braille.css.PropertyValue;
import org.daisy.pipeline.css.CssSerializer;

public final class BrailleCssSerializer {

	private BrailleCssSerializer() {}

	/* =================================================== */
	/* toString                                            */
	/* =================================================== */

	public static String toString(Term<?> term) {
		if (term instanceof ContentList ||
		    term instanceof StringSetList ||
		    term instanceof CounterSetList)
			return serializeTermList((List<Term<?>>)term);
		else if (term instanceof AttrFunction) {
			AttrFunction f = (AttrFunction)term;
			String s = "attr(" + toString(f.name);
			if (f.asURL)
				s = s + " url";
			s += ")";
			return s; }
		else if (term instanceof ContentFunction) {
			ContentFunction f = (ContentFunction)term;
			if (f.target.isPresent())
				return "target-content(" + toString(f.target.get()) + ")";
			else
				return "content()"; }
		else if (term instanceof StringFunction) {
			StringFunction f = (StringFunction)term;
			if (f.target.isPresent())
				return "target-string(" + toString(f.target.get()) + ", " + toString(f.name) + ")";
			else {
				String s = "string(" + toString(f.name);
				if (f.scope.isPresent())
					s = s + ", " + f.scope.get().toString();
				s += ")";
				return s; }}
		else if (term instanceof CounterFunction) {
			CounterFunction f = (CounterFunction)term;
			String s = "";
			if (f.target.isPresent())
				s = "target-counter(" + toString(f.target.get()) + ", " + toString(f.name);
			else
				s = "counter(" + toString(f.name);
			if (f.style.isPresent())
				s = s + ", " + toString(f.style.get());
			s += ")";
			return s; }
		else if (term instanceof TextFunction) {
			TextFunction f = (TextFunction)term;
			return "target-text(" + toString(f.target) + ")"; }
		else if (term instanceof LeaderFunction) {
			LeaderFunction f = (LeaderFunction)term;
			String s = "leader(" + toString(f.pattern);
			if (f.position.isPresent())
				s = s + ", " + toString(f.position.get());
			if (f.alignment.isPresent())
				s = s + ", " + f.alignment.get().toString();
			s += ")";
			return s; }
		else if (term instanceof FlowFunction) {
			FlowFunction f = (FlowFunction)term;
			String s = "flow(" + toString(f.from);
			if (f.scope.isPresent())
				s = s + ", " + f.scope.get().toString();
			s += ")";
			return s; }
		else
			return CssSerializer.toString(term, t -> toString(t));
	}

	public static String toString(Declaration declaration) {
		return declaration.getProperty() + ": " + serializeTermList((List<Term<?>>)declaration);
	}

	public static String toString(BrailleCssStyle style) {
		return toString(style, null);
	}

	private static String toString(BrailleCssStyle style, String base) {
		StringBuilder b = new StringBuilder();
		StringBuilder rel = new StringBuilder();
		if (style.simpleStyle != null)
			b.append(toString(style.simpleStyle));
		else if (style.declarations != null)
			b.append(serializeDeclarationList(style.declarations));
		if (style.nestedStyles != null)
			for (Map.Entry<String,BrailleCssStyle> e : style.nestedStyles.entrySet()) {
				if (base != null && e.getKey().startsWith("&")) {
					if (rel.length() > 0) rel.append(" ");
					rel.append(toString(e.getValue(), base + e.getKey().substring(1)));
				} else {
					if (b.length() > 0) {
						if (b.charAt(b.length() - 1) != '}') b.append(";");
						b.append(" ");
					}
					b.append(toString(e.getValue(), e.getKey()));
				}
			}
		if (base != null && b.length() > 0) {
			b.insert(0, base + " { ");
			b.append(" }");
		}
		if (rel.length() > 0) {
			if (b.length() > 0) {
				if (b.charAt(b.length() - 1) != '}') b.append(";");
				b.append(" ");
			}
			b.append(rel);
		}
		return b.toString();
	}

	public static String toString(InlineStyle style) {
		return toString(BrailleCssStyle.of(style, Context.ELEMENT));
	}

	public static String toString(NodeData style) {
		List<String> declarations = new ArrayList<>();
		for (String p : style.getPropertyNames()) {
			String v = serializePropertyValue(style, p);
			if (v != null)
				declarations.add(p + ": " + v);
		}
		Collections.sort(declarations);
		StringBuilder s = new StringBuilder();
		Iterator<String> it = declarations.iterator();
		while (it.hasNext()) {
			s.append(it.next());
			if (it.hasNext()) s.append("; ");
		}
		return s.toString();
	}

	public static String serializePropertyValue(NodeData style, String property) {
		return serializePropertyValue(style, property, true);
	}

	public static String serializePropertyValue(NodeData style, String property, boolean includeInherited) {
		Term<?> value = style.getValue(property, includeInherited);
		if (value != null)
			return toString(value);
		else {
			CSSProperty p = style.getProperty(property, includeInherited);
			return p != null ? p.toString() : null;
		}
	}

	public static String serializePropertyValue(PropertyValue propValue) {
		Term<?> value = propValue.getValue();
		if (value != null)
			return toString(value);
		else
			return propValue.getCSSProperty().toString();
	}

	public static String toString(RuleMainBlock rule) {
		return serializeDeclarationList(rule);
	}

	public static String toString(RuleRelativeBlock rule) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Selector s : rule.getSelector()) {
			Combinator c = s.getCombinator();
			if (first) {
				if (c == null)
					b.append("&");
				else if (c != Combinator.CHILD)
					b.append(c.value());
				first = false;
			} else if (c != null) // should always be true
				b.append(c.value());
			b = OutputUtil.appendList(b, s, OutputUtil.EMPTY_DELIM);
		}
		b.append(" { ");
		b.append(toString((RuleBlock<Rule<?>>)rule));
		b.append(" }");
		return b.toString();
	}

	public static String toString(RuleBlock<? extends Rule<?>> ruleBlock) {
		StringBuilder b = new StringBuilder();
		b.append(serializeDeclarationList(Iterables.filter(ruleBlock, Declaration.class)));
		for (Rule<?> r : ruleBlock)
			if (r instanceof Declaration);
			else if (r instanceof RulePage)
				b.append(toString((RulePage)r));
			else
				throw new RuntimeException("not implemented");
		return b.toString();
	}

	public static String toString(RulePage page, SupportedBrailleCSS supportedCss) {
		return toString(BrailleCssStyle.of(page, supportedCss));
	}

	public static String serializeRuleBlockList(Iterable<? extends RuleBlock<? extends Rule<?>>> ruleBlocks) {
		String b = null;
		for (RuleBlock<? extends Rule<?>> r : ruleBlocks) {
			String s;
			if (r instanceof RuleMainBlock)
				s = BrailleCssSerializer.toString((RuleMainBlock)r);
			else if (r instanceof RuleRelativeBlock)
				s = BrailleCssSerializer.toString((RuleRelativeBlock)r);
			else
				s = BrailleCssSerializer.toString(r);
			if (!s.isEmpty())
				if (b == null)
					b = s;
				else {
					if (!(b.endsWith("}") || b.endsWith(";")))
						b = b + ";";
					b += " ";
					b += s; }}
		if (b == null) b = "";
		return b;
	}

	public static String serializeTermList(List<Term<?>> termList) {
		return CssSerializer.serializeTermList(termList, t -> toString(t));
	}

	public static String serializeLanguageRanges(List<LanguageRange> languageRanges) {
		return OutputUtil.appendList(new StringBuilder(), languageRanges, OutputUtil.SELECTOR_DELIM).toString();
	}

	public static String serializeDeclarationList(Iterable<? extends Declaration> declarations) {
		List<String> sortedDeclarations = new ArrayList<>();
		for (Declaration d : declarations)
			sortedDeclarations.add(BrailleCssSerializer.toString(d));
		Collections.sort(sortedDeclarations);
		StringBuilder s = new StringBuilder();
		Iterator<String> it = sortedDeclarations.iterator();
		while (it.hasNext()) {
			s.append(it.next());
			if (it.hasNext()) s.append("; ");
		}
		return s.toString();
	}

	/* = PRIVATE ========================================= */

	private static String toString(URL url) {
		if (url.url != null)
			return toString(url.url);
		else
			return toString(url.urlAttr);
	}

	private static String toString(CounterStyle style) {
		if (style.name != null)
			return style.name;
		else if (style.symbol != null)
			return toString(style.symbol);
		else if (style.symbols != null)
			return toString(style.symbols);
		else
			return "none";
	}

	/* =================================================== */
	/* toXml                                               */
	/* =================================================== */

	private static final String XMLNS_CSS       = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final QName CSS_RULE         = new QName(XMLNS_CSS, "rule",     "css");
	private static final QName CSS_PROPERTY     = new QName(XMLNS_CSS, "property", "css");
	private static final QName CSS_STRING       = new QName(XMLNS_CSS, "string",   "css");
	private static final QName CSS_CONTENT      = new QName(XMLNS_CSS, "content",  "css");
	private static final QName CSS_ATTR         = new QName(XMLNS_CSS, "attr",     "css");
	private static final QName CSS_COUNTER      = new QName(XMLNS_CSS, "counter",  "css");
	private static final QName CSS_TEXT         = new QName(XMLNS_CSS, "text",     "css");
	private static final QName CSS_LEADER       = new QName(XMLNS_CSS, "leader",   "css");
	private static final QName CSS_FLOW         = new QName(XMLNS_CSS, "flow", "css");
	private static final QName CSS_CUSTOM_FUNC  = new QName(XMLNS_CSS, "custom-func", "css");
	private static final QName CSS_STRING_SET   = new QName(XMLNS_CSS, "string-set", "css");
	private static final QName CSS_COUNTER_SET  = new QName(XMLNS_CSS, "counter-set", "css");
	private static final QName SELECTOR         = new QName("selector");
	private static final QName NAME             = new QName("name");
	private static final QName VALUE            = new QName("value");
	private static final QName STYLE            = new QName("style");
	private static final QName TARGET           = new QName("target");
	private static final QName TARGET_ATTRIBUTE = new QName("target-attribute");
	private static final QName SCOPE            = new QName("scope");
	private static final QName PATTERN          = new QName("pattern");
	private static final QName POSITION         = new QName("position");
	private static final QName ALIGNMENT        = new QName("alignment");
	private static final QName FROM             = new QName("from");

	public static void toXml(BrailleCssStyle style, XMLStreamWriter writer) throws XMLStreamException {
		toXml(style, writer, false);
	}

	public static void toXml(BrailleCssStyle style,
	                         XMLStreamWriter w,
	                         boolean recursive) throws XMLStreamException {
		if (style.simpleStyle != null || style.declarations != null) {
			if (!recursive || style.nestedStyles != null)
				writeStartElement(w, CSS_RULE);
			if (style.simpleStyle != null) {
				List<String> properties = new ArrayList<>();
				properties.addAll(style.simpleStyle.getPropertyNames());
				Collections.sort(properties);
				for (String p : properties)
					toXml(p, style.simpleStyle.get(p), w);
			} else {
				List<Declaration> declarations = new ArrayList<>();
				declarations.addAll(style.declarations);
				Collections.sort(declarations, Ordering.natural().onResultOf(Declaration::getProperty));
				for (Declaration d : declarations) {
					writeStartElement(w, CSS_PROPERTY);
					writeAttribute(w, NAME, d.getProperty());
					writeAttribute(w, VALUE, serializeTermList((List<Term<?>>)d));
					w.writeEndElement();
				}
			}
			if (!recursive || style.nestedStyles != null)
				w.writeEndElement();
		}
		if (style.nestedStyles != null)
			for (Map.Entry<String,BrailleCssStyle> e : style.nestedStyles.entrySet()) {
				writeStartElement(w, CSS_RULE);
				writeAttribute(w, SELECTOR, e.getKey());
				toXml(e.getValue(), w, true);
				w.writeEndElement();
			}
	}

	private static void toXml(String property, PropertyValue value, XMLStreamWriter writer) throws XMLStreamException {
		writeStartElement(writer, CSS_PROPERTY);
		writeAttribute(writer, NAME, property);
		if ("content".equals(property)) {
			if (value.getCSSProperty() == Content.NONE || value.getCSSProperty() == Content.INITIAL)
				;
			else if (value.getCSSProperty() == Content.INHERIT)
				writeAttribute(writer, VALUE, Content.INHERIT.toString());
			else if (value.getCSSProperty() == Content.content_list)
				if (value.getValue() instanceof ContentList)
					contentListToXml((ContentList)value.getValue(), writer);
				else
					throw new IllegalArgumentException();
		} else if ("string-set".equals(property)) {
			if (value.getCSSProperty() == StringSet.NONE || value.getCSSProperty() == StringSet.INITIAL)
				;
			else if (value.getCSSProperty() == StringSet.INHERIT)
				writeAttribute(writer, VALUE, StringSet.INHERIT.toString());
			else if (value.getCSSProperty() == StringSet.list_values)
				if (value.getValue() instanceof StringSetList)
					for (StringSetList.StringSet ss : (StringSetList)value.getValue()) {
						writeStartElement(writer, CSS_STRING_SET);
						writeAttribute(writer, NAME, ss.getKey());
						contentListToXml(ss.getValue(), writer);
						writer.writeEndElement(); }
				else
					throw new IllegalArgumentException();
		} else if ("counter-set".equals(property) ||
		           "counter-reset".equals(property) ||
		           "counter-increment".equals(property)) {
			if (value.getCSSProperty() == CounterSet.NONE || value.getCSSProperty() == CounterSet.INITIAL ||
			    value.getCSSProperty() == CounterReset.NONE || value.getCSSProperty() == CounterReset.INITIAL ||
			    value.getCSSProperty() == CounterIncrement.NONE || value.getCSSProperty() == CounterIncrement.INITIAL)
				;
			else if (value.getCSSProperty() == CounterSet.INHERIT ||
			         value.getCSSProperty() == CounterReset.INHERIT ||
			         value.getCSSProperty() == CounterIncrement.INHERIT)
				writeAttribute(writer, VALUE, "inherit");
			else if (value.getCSSProperty() == CounterSet.list_values ||
			         value.getCSSProperty() == CounterReset.list_values ||
			         value.getCSSProperty() == CounterIncrement.list_values)
				if (value.getValue() instanceof CounterSetList)
					for (CounterSetList.CounterSet ss : (CounterSetList)value.getValue()) {
						writeStartElement(writer, CSS_COUNTER_SET);
						writeAttribute(writer, NAME, ss.getKey());
						writeAttribute(writer, VALUE, "" + ss.getValue());
						writer.writeEndElement(); }
				else
					throw new IllegalArgumentException();
		} else
			writeAttribute(writer, VALUE, serializePropertyValue(value));
		writer.writeEndElement();
	}

	private static void contentListToXml(ContentList list, XMLStreamWriter w) throws XMLStreamException {
		for (Term<?> i : list)
			if (i instanceof TermString || i instanceof TermURI) {
				Term<String> s = (Term<String>)i;
				writeStartElement(w, CSS_STRING);
				writeAttribute(w, VALUE, s.getValue());
				w.writeEndElement(); }
			else if (i instanceof AttrFunction) {
				AttrFunction f = (AttrFunction)i;
				writeStartElement(w, CSS_ATTR);
				writeAttribute(w, NAME, f.name.getValue());
				w.writeEndElement(); }
			else if (i instanceof ContentFunction) {
				ContentFunction f = (ContentFunction)i;
				writeStartElement(w, CSS_CONTENT);
				if (f.target.isPresent())
					if (f.target.get().url != null) {
						TermURI t = f.target.get().url;
						URI url = URLs.asURI(t.getValue());
						if (t.getBase() != null)
							url = URLs.resolve(URLs.asURI(t.getBase()), url);
						writeAttribute(w, TARGET, url.toString());
					} else
						writeAttribute(w, TARGET_ATTRIBUTE, f.target.get().urlAttr.name.getValue());
				w.writeEndElement(); }
			else if (i instanceof StringFunction) {
				StringFunction f = (StringFunction)i;
				writeStartElement(w, CSS_STRING);
				writeAttribute(w, NAME, f.name.getValue());
				if (f.target.isPresent())
					if (f.target.get().url != null) {
						TermURI t = f.target.get().url;
						URI url = URLs.asURI(t.getValue());
						if (t.getBase() != null)
							url = URLs.resolve(URLs.asURI(t.getBase()), url);
						writeAttribute(w, TARGET, url.toString());
					} else
						writeAttribute(w, TARGET_ATTRIBUTE, f.target.get().urlAttr.name.getValue());
				else if (f.scope.isPresent())
					writeAttribute(w, SCOPE, f.scope.get().toString());
				w.writeEndElement(); }
			else if (i instanceof CounterFunction) {
				CounterFunction f = (CounterFunction)i;
				writeStartElement(w, CSS_COUNTER);
				writeAttribute(w, NAME, f.name.getValue());
				if (f.target.isPresent())
					if (f.target.get().url != null) {
						TermURI t = f.target.get().url;
						URI url = URLs.asURI(t.getValue());
						if (t.getBase() != null)
							url = URLs.resolve(URLs.asURI(t.getBase()), url);
						writeAttribute(w, TARGET, url.toString());
					} else
						writeAttribute(w, TARGET_ATTRIBUTE, f.target.get().urlAttr.name.getValue());
				if (f.style.isPresent())
					writeAttribute(w, STYLE, toString(f.style.get()));
				w.writeEndElement(); }
			else if (i instanceof TextFunction) {
				TextFunction f = (TextFunction)i;
				writeStartElement(w, CSS_TEXT);
				if (f.target.url != null) {
					TermURI t = f.target.url;
					URI url = URLs.asURI(t.getValue());
					if (t.getBase() != null)
						url = URLs.resolve(URLs.asURI(t.getBase()), url);
					writeAttribute(w, TARGET, url.toString());
				} else
					writeAttribute(w, TARGET_ATTRIBUTE, f.target.urlAttr.name.getValue());
				w.writeEndElement(); }
			else if (i instanceof LeaderFunction) {
				LeaderFunction f = (LeaderFunction)i;
				writeStartElement(w, CSS_LEADER);
				writeAttribute(w, PATTERN, f.pattern.getValue());
				if (f.position.isPresent())
					writeAttribute(w, POSITION, toString(f.position.get()));
				if (f.alignment.isPresent())
					writeAttribute(w, ALIGNMENT, f.alignment.get().toString());
				w.writeEndElement(); }
			else if (i instanceof FlowFunction) {
				FlowFunction f = (FlowFunction)i;
				writeStartElement(w, CSS_FLOW);
				writeAttribute(w, FROM, f.from.getValue());
				if (f.scope.isPresent())
					writeAttribute(w, SCOPE, f.scope.get().toString());
				w.writeEndElement(); }
			else if (i instanceof TermFunction) {
				TermFunction f = (TermFunction)i;
				writeStartElement(w, CSS_CUSTOM_FUNC);
				writeAttribute(w, NAME, f.getFunctionName());
				int k = 0;
				for (Term<?> arg : f) {
					k++;
					writeAttribute(w, new QName("arg" + k), toString(arg)); }
				w.writeEndElement(); }
			else
				throw new RuntimeException("coding error");
	}
}
