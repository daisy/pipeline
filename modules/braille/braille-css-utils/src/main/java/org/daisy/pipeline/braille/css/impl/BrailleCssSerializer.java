package org.daisy.pipeline.braille.css.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
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
import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser.ParsedDeclaration;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser.ParsedDeclarations;
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

public class BrailleCssSerializer extends CssSerializer {

	private static BrailleCssSerializer INSTANCE = new BrailleCssSerializer();

	public static BrailleCssSerializer getInstance() {
		return INSTANCE;
	}

	/* =================================================== */
	/* toString                                            */
	/* =================================================== */

	@Override
	public String toString(Term<?> term) {
		if (term instanceof TextTransformList ||
		    term instanceof ContentList ||
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
			return super.toString(term);
	}

	public String toString(Declaration declaration) {
		if (declaration instanceof PropertyValue)
			return declaration.getProperty() + ": " + serializePropertyValue((PropertyValue)declaration);
		else
			return declaration.getProperty() + ": " + serializeTermList(declaration);
	}

	public String toString(BrailleCssStyle style) {
		if (style.serialized == null) {
			style.serialized = toString(style, null, null);
			// cache
			if (style.parser != null)
				style.parser.cache.put(style.context, style.serialized, style);
		} else {
			// access cache to keep entry longer in it
			if (style.parser != null)
				style.parser.cache.get(style.context, style.serialized);
		}
		return style.serialized;
	}

	/**
	 * @param relativeTo If not {@code null}, include only those declarations that are needed
	 *                   to reconstruct {@code style} with {@code relativeTo} as the parent
	 *                   style. Relativizes even if the parent style is empty.
	 */
	public String toString(BrailleCssStyle style, BrailleCssStyle relativeTo) {
		if (relativeTo == null)
			return toString(style);
		String s = toString(style.relativize(relativeTo));
		// cache
		if (style.parser != null)
			style.parser.cache.put(style.context,
			                       s,
			                       relativeTo.declarations != null
			                           ? (ParsedDeclarations)relativeTo.declarations
			                           : ParsedDeclarations.EMPTY,
			                       true,
			                       style);
		return s;
	}

	public String toString(BrailleCssStyle style, String indentation) {
		// this function does update the cache, nor does it store the string in the style object, as
		// it's meant to pretty print a style (for the purpose of showing in temporary files or log
		//messages)
		return toString(style, null, indentation);
	}

	String toString(BrailleCssStyle style, String base, String indent) {
		StringBuilder b = new StringBuilder();
		StringBuilder rel = new StringBuilder();
		if ("".equals(indent)) indent = null;
		String newline = indent != null ? "\n" : " ";
		if (style.declarations != null) {
			b.append(serializeDeclarationList(style.declarations, ";" + newline));
			if (indent != null && b.length() > 0) b.append(";");
		}
		if (style.nestedStyles != null)
			for (Map.Entry<String,BrailleCssStyle> e : style.nestedStyles.entrySet()) {
				if (base != null && e.getKey().startsWith("&")) {
					if (rel.length() > 0) rel.append(newline);
					rel.append(toString(e.getValue(), base + e.getKey().substring(1), indent));
				} else {
					if (b.length() > 0) {
						if (indent == null && b.charAt(b.length() - 1) != '}') b.append(";");
						b.append(newline);
					}
					b.append(toString(e.getValue(), e.getKey(), indent));
				}
			}
		if (base != null && b.length() > 0) {
			if (indent != null) {
				String s = b.toString();
				s = indent + s.trim().replaceAll("\n", "\n" + indent);
				b.setLength(0);
				b.append(s);
			}
			b.insert(0, base + " {" + newline);
			b.append(newline + "}");
		}
		if (rel.length() > 0) {
			if (b.length() > 0) {
				if (indent == null && b.charAt(b.length() - 1) != '}') b.append(";");
				b.append(newline);
			}
			b.append(rel);
		}
		return b.toString();
	}

	public String toString(NodeData style) {
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

	public String serializePropertyValue(NodeData style, String property) {
		return serializePropertyValue(style, property, true);
	}

	public String serializePropertyValue(NodeData style, String property, boolean includeInherited) {
		Term<?> value = style.getValue(property, includeInherited);
		if (value != null)
			return toString(value);
		else {
			CSSProperty p = style.getProperty(property, includeInherited);
			return p != null ? p.toString() : null;
		}
	}

	public String serializePropertyValue(PropertyValue propValue) {
		if (propValue instanceof ParsedDeclaration)
			return ((ParsedDeclaration)propValue).valueToString(); // this may trigger caching
		else {
			Term<?> value = propValue.getValue();
			if (value != null)
				return toString(value);
			else
				return propValue.getCSSProperty().toString();
		}
	}

	public String toString(RuleMainBlock rule) {
		return serializeDeclarationList(rule);
	}

	public String toString(RuleRelativeBlock rule) {
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

	public String toString(RuleBlock<? extends Rule<?>> ruleBlock) {
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

	public String toString(RulePage page, BrailleCssParser parser) {
		return toString(BrailleCssStyle.of(parser, page));
	}

	public String serializeRuleBlockList(Iterable<? extends RuleBlock<? extends Rule<?>>> ruleBlocks) {
		String b = null;
		for (RuleBlock<? extends Rule<?>> r : ruleBlocks) {
			String s;
			if (r instanceof RuleMainBlock)
				s = toString((RuleMainBlock)r);
			else if (r instanceof RuleRelativeBlock)
				s = toString((RuleRelativeBlock)r);
			else
				s = toString(r);
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

	public String serializeLanguageRanges(List<LanguageRange> languageRanges) {
		return OutputUtil.appendList(new StringBuilder(), languageRanges, OutputUtil.SELECTOR_DELIM).toString();
	}

	public String serializeDeclarationList(Iterable<? extends Declaration> declarations) {
		return serializeDeclarationList(declarations, "; ");
	}

	private String serializeDeclarationList(Iterable<? extends Declaration> declarations, String separator) {
		List<String> sortedDeclarations = new ArrayList<>();
		for (Declaration d : declarations)
			sortedDeclarations.add(toString(d));
		Collections.sort(sortedDeclarations);
		StringBuilder s = new StringBuilder();
		Iterator<String> it = sortedDeclarations.iterator();
		while (it.hasNext()) {
			s.append(it.next());
			if (it.hasNext()) s.append(separator);
		}
		return s.toString();
	}

	/* = PRIVATE ========================================= */

	private String toString(URL url) {
		if (url.url != null)
			return toString(url.url);
		else
			return toString(url.urlAttr);
	}

	private String toString(CounterStyle style) {
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
	/* toAttributes                                        */
	/* =================================================== */

	public void toAttributes(BrailleCssStyle style, XMLStreamWriter writer) throws XMLStreamException {
		if (style.nestedStyles != null)
			throw new UnsupportedOperationException();
		if (style.declarations != null)
			for (Declaration d : style.declarations)
				toAttribute(d, writer);
	}

	/**
	 * @param relativeTo If not {@code null}, include only those declarations that are needed
	 *                   to reconstruct {@code style} with {@code relativeTo} as the parent
	 *                   style. Relativizes even if the parent style is empty.
	 */
	public void toAttributes(BrailleCssStyle style, BrailleCssStyle relativeTo, XMLStreamWriter writer)
			throws XMLStreamException {
		if (relativeTo == null)
			toAttributes(style, writer);
		else
			toAttributes(style.relativize(relativeTo), writer);
	}

	private void toAttribute(Declaration declaration, XMLStreamWriter writer) throws XMLStreamException {
		writeAttribute(writer,
		               new QName(XMLNS_CSS, declaration.getProperty().replaceAll("^-", "_"), "css"),
		               declaration instanceof PropertyValue
		                   ? serializePropertyValue((PropertyValue)declaration)
		                   : serializeTermList(declaration));
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

	public void toXml(BrailleCssStyle style, XMLStreamWriter writer) throws XMLStreamException {
		toXml(style, writer, false);
	}

	private void toXml(BrailleCssStyle style,
	                   XMLStreamWriter w,
	                   boolean recursive) throws XMLStreamException {
		if (style.declarations != null && !Iterables.isEmpty(style.declarations)) {
			if (!recursive || style.nestedStyles != null)
				writeStartElement(w, CSS_RULE);
			List<Declaration> declarations = new ArrayList<>();
			for (Declaration d : style.declarations) declarations.add(d);
			Collections.sort(declarations, Ordering.natural().onResultOf(Declaration::getProperty));
			for (Declaration d : declarations)
				toXml(d, w);
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

	public void toXml(Declaration declaration, XMLStreamWriter writer) throws XMLStreamException {
		if (declaration instanceof PropertyValue)
			toXml((PropertyValue)declaration, writer);
		else {
			writeStartElement(writer, CSS_PROPERTY);
			writeAttribute(writer, NAME, declaration.getProperty());
			writeAttribute(writer, VALUE, serializeTermList(declaration));
			writer.writeEndElement();
		}
	}

	public void toXml(PropertyValue value, XMLStreamWriter writer) throws XMLStreamException {
		writeStartElement(writer, CSS_PROPERTY);
		writeAttribute(writer, NAME, value.getProperty());
		CSSProperty p = value.getCSSProperty();
		if (p == Content.NONE || p == Content.INITIAL)
			;
		else if (p == Content.INHERIT)
			writeAttribute(writer, VALUE, Content.INHERIT.toString());
		else if (p == Content.content_list) {
			if (value.getValue() instanceof ContentList)
				toXml((ContentList)value.getValue(), writer);
			else
				throw new IllegalArgumentException();
		} else if (p == StringSet.NONE || p == StringSet.INITIAL)
			;
		else if (p == StringSet.INHERIT)
			writeAttribute(writer, VALUE, StringSet.INHERIT.toString());
		else if (p == StringSet.list_values)
			if (value.getValue() instanceof StringSetList)
				toXml((StringSetList)value.getValue(), writer);
			else
				throw new IllegalArgumentException();
		else if (p == CounterSet.NONE || p == CounterSet.INITIAL ||
		         p == CounterReset.NONE || p == CounterReset.INITIAL ||
		         p == CounterIncrement.NONE || p == CounterIncrement.INITIAL)
			;
		else if (p == CounterSet.INHERIT ||
		         p == CounterReset.INHERIT ||
		         p == CounterIncrement.INHERIT)
			writeAttribute(writer, VALUE, "inherit");
		else if (p == CounterSet.list_values ||
		         p == CounterReset.list_values ||
		         p == CounterIncrement.list_values)
			if (value.getValue() instanceof CounterSetList)
				toXml((CounterSetList)value.getValue(), writer);
			else
				throw new IllegalArgumentException();
		else
			writeAttribute(writer, VALUE, serializePropertyValue(value));
		writer.writeEndElement();
	}

	public void toXml(ContentList list, XMLStreamWriter w) throws XMLStreamException {
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

	public void toXml(StringSetList list, XMLStreamWriter w) throws XMLStreamException {
		for (Term<?> i : list) {
			if (i instanceof StringSetList.StringSet) {
				StringSetList.StringSet s = (StringSetList.StringSet)i;
				writeStartElement(w, CSS_STRING_SET);
				writeAttribute(w, NAME, s.getKey());
				toXml(s.getValue(), w);
				w.writeEndElement(); }
			else
				throw new RuntimeException("coding error");
		}
	}

	public void toXml(CounterSetList list, XMLStreamWriter w) throws XMLStreamException {
		for (Term<?> i : list) {
			if (i instanceof CounterSetList.CounterSet) {
				CounterSetList.CounterSet s = (CounterSetList.CounterSet)i;
				writeStartElement(w, CSS_COUNTER_SET);
				writeAttribute(w, NAME, s.getKey());
				writeAttribute(w, VALUE, "" + s.getValue());
				w.writeEndElement(); }
			else
				throw new RuntimeException("coding error");
		}
	}
}
