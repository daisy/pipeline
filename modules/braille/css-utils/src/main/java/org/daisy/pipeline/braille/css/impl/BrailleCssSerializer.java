package org.daisy.pipeline.braille.css.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterables;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.Combinator;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermNumber;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.csskit.OutputUtil;

import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.PropertyValue;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.util.Strings;

public final class BrailleCssSerializer {

	private BrailleCssSerializer() {}

	/* =================================================== */
	/* toString                                            */
	/* =================================================== */

	public static String toString(Term<?> term) {
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
		else if (term instanceof TermList
		         || term instanceof Declaration) {
			String s = serializeTermList((List<Term<?>>)term);
			if (term instanceof TermFunction) {
				TermFunction function = (TermFunction)term;
				s = function.getFunctionName() + "(" + s + ")"; }
			return s; }
		else if (term instanceof TermPair) {
			TermPair<?,?> pair = (TermPair<?,?>)term;
			Object val = pair.getValue();
			return "" + pair.getKey() + " " + (val instanceof Term ? toString((Term<?>)val) : val.toString()); }
		else if (term instanceof TermString) {
			TermString string = (TermString)term;
			return "'" + string.getValue().replaceAll("\n", "\\\\A ").replaceAll("'", "\\\\27 ") + "'"; }
		else
			return term.toString().replaceAll("^[,/ ]+", "");
	}

	public static String toString(Declaration declaration) {
		return declaration.getProperty() + ": " + serializeTermList((List<Term<?>>)declaration) + ";";
	}

	public static String toString(BrailleCssTreeBuilder.Style style) {
		return toString(style, null);
	}

	private static String toString(BrailleCssTreeBuilder.Style style, String base) {
		StringBuilder b = new StringBuilder();
		StringBuilder rel = new StringBuilder();
		if (style.declarations != null)
			b.append(serializeDeclarationList2(style.declarations));
		if (style.nestedStyles != null)
			for (Map.Entry<String,BrailleCssTreeBuilder.Style> e : style.nestedStyles.entrySet()) {
				if (base != null && e.getKey().startsWith("&")) {
					if (rel.length() > 0) rel.append(" ");
					rel.append(toString(e.getValue(), base + e.getKey().substring(1)));
				} else {
					if (b.length() > 0) b.append(" ");
					b.append(toString(e.getValue(), e.getKey()));
				}
			}
		if (base != null && b.length() > 0) {
			b.insert(0, base + " { ");
			b.append(" }");
		}
		if (rel.length() > 0) {
			if (b.length() > 0) b.append(" ");
			b.append(rel);
		}
		return b.toString();
	}

	public static String toString(InlineStyle style) {
		return toString(BrailleCssTreeBuilder.Style.of(style));
	}

	public static String toString(SimpleInlineStyle style) {
		List<String> declarations = new ArrayList<>();
		for (String p : style.getPropertyNames())
			declarations.add(p + ": " + serializePropertyValue(style.get(p)));
		Collections.sort(declarations);
		return Strings.join(declarations, "; ");
	}

	public static String serializePropertyValue(PropertyValue propValue) {
		Term<?> value = propValue.getValue();
		if (value != null)
			return toString(value);
		else
			return propValue.getProperty().toString();
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

	public static String toString(RulePage page) {
		return toString(
			new BrailleCssTreeBuilder.Style().add(
				"@page",
				BrailleCssTreeBuilder.Style.of(page)));
	}

	/* =================================================== */

	private static String serializeTermList(List<Term<?>> termList) {
		String s = "";
		for (Term<?> t : termList) {
			if (!s.isEmpty()) {
				Term.Operator o = t.getOperator();
				if (o != null)
					switch (o) {
					case COMMA:
						s += ","; }
				s += " "; }
			s += toString(t); }
		return s;
	}

	private static String serializeDeclarationList(Iterable<Declaration> declarations) {
		List<Declaration> sortedDeclarations = new ArrayList<Declaration>();
		for (Declaration d : declarations) sortedDeclarations.add(d);
		Collections.sort(sortedDeclarations);
		return Strings.join(sortedDeclarations, "; ", d -> d.getProperty() + ": " + serializeTermList((List<Term<?>>)d));
	}

	private static String serializeDeclarationList2(List<Declaration> declarations) {
		List<Declaration> sortedDeclarations = new ArrayList<Declaration>(declarations);
		Collections.sort(sortedDeclarations);
		return Strings.join(sortedDeclarations, " ", BrailleCssSerializer::toString);
	}

	/* =================================================== */
	/* toXml                                               */
	/* =================================================== */

	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final QName CSS_RULE = new QName(XMLNS_CSS, "rule", "css");
	private static final QName CSS_PROPERTY = new QName(XMLNS_CSS, "property", "css");
	private static final QName SELECTOR = new QName("selector");
	private static final QName STYLE = new QName("style");
	private static final QName NAME = new QName("name");
	private static final QName VALUE = new QName("value");

	public static void toXml(BrailleCssTreeBuilder.Style style, XMLStreamWriter writer, boolean deep) throws XMLStreamException {
		toXml(style, writer, deep, false);
	}

	private static void toXml(BrailleCssTreeBuilder.Style style,
	                          XMLStreamWriter w,
	                          boolean deep,
	                          boolean recursive) throws XMLStreamException {
		if (style.declarations != null) {
			if (!deep || !recursive || style.nestedStyles != null)
				writeStartElement(w, CSS_RULE);
			if (!deep)
				writeAttribute(w, STYLE, serializeDeclarationList2(style.declarations));
			if (deep) {
				for (Declaration d : style.declarations) {
					writeStartElement(w, CSS_PROPERTY);
					writeAttribute(w, NAME, d.getProperty());
					writeAttribute(w, VALUE, serializeTermList((List<Term<?>>)d));
					w.writeEndElement();
				}
			}
			if (!deep || !recursive || style.nestedStyles != null)
				w.writeEndElement();
		}
		if (style.nestedStyles != null)
			for (Map.Entry<String,BrailleCssTreeBuilder.Style> e : style.nestedStyles.entrySet()) {
				writeStartElement(w, CSS_RULE);
				writeAttribute(w, SELECTOR, e.getKey());
				if (!deep)
					writeAttribute(w, STYLE, e.getValue().toString());
				if (deep)
					toXml(e.getValue(), w, true, true);
				w.writeEndElement();
			}
	}
}
