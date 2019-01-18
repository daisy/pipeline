package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Function;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermString;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;

import org.daisy.pipeline.braille.common.util.Strings;
import static org.daisy.pipeline.braille.common.util.Functions.propagateException;
import org.daisy.braille.css.AnyAtRule;
import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativeBlock;
import org.daisy.braille.css.InlineStyle.RuleRelativePage;
import org.daisy.braille.css.InlineStyle.RuleRelativeVolume;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.RuleVolumeArea;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.common.saxon.SaxonHelper;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.stax.XMLStreamWriterHelper.XMLStreamWritable;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "css:parse-stylesheet",
	service = { ExtensionFunctionDefinition.class }
)
public class ParseStylesheetDefinition extends ExtensionFunctionDefinition {
	
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	
	private static final StructuredQName funcname = new StructuredQName("css", XMLNS_CSS, "parse-stylesheet");
	
	private static final QName CSS_RULE = new QName(XMLNS_CSS, "rule", "css");
	private static final QName CSS_PROPERTY = new QName(XMLNS_CSS, "property", "css");
	private static final QName SELECTOR = new QName("selector");
	private static final QName STYLE = new QName("style");
	private static final QName NAME = new QName("name");
	private static final QName VALUE = new QName("value");
	private static final QName PAGE = new QName("page");
	private static final QName VOLUME = new QName("volume");
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 1;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 3;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.OPTIONAL_STRING,
			SequenceType.SINGLE_BOOLEAN,
			SequenceType.OPTIONAL_QNAME,
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.NODE_SEQUENCE;
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				if (arguments.length == 0)
					return EmptySequence.getInstance();
				Item arg = arguments[0].head();
				if (arg == null)
					return EmptySequence.getInstance();
				boolean deep = arguments.length > 1
					? ((BooleanValue)arguments[1]).getBooleanValue()
					: false;
				Context styleCtxt = Context.ELEMENT; {
					if (arguments.length > 2) {
						Item i = arguments[2].head();
						if (i != null) {
							QName qn = ((QNameValue)i).toJaxpQName();
							if (qn.equals(PAGE))
								styleCtxt = Context.PAGE;
							else if (qn.equals(VOLUME))
								styleCtxt = Context.VOLUME;
							else
								throw new RuntimeException(); }}}
				List<NodeInfo> result = new ArrayList<>();
				try {
					Style style = new Style();
					for (RuleBlock<?> rule : new InlineStyle(arg.getStringValue(), styleCtxt)) {
						if (rule instanceof RuleMainBlock)
							style.add((List<Declaration>)rule);
						else if (rule instanceof RuleRelativeBlock) {
							String[] selector = serializeSelector((RuleRelativeBlock)rule);
							List<Declaration> decls = (List<Declaration>)rule;
							style.add(selector[0],
							          selector.length == 2
							              ? new Style().add(selector[1], new Style().add(decls))
							              : new Style().add(decls)); }
						else if (rule instanceof RulePage)
							style.add("@page", Style.of((RulePage)rule));
						else if (rule instanceof RuleVolume)
							style.add("@volume", Style.of((RuleVolume)rule));
						else if (rule instanceof RuleTextTransform)
							style.add("@text-transform " + ((RuleTextTransform)rule).getName(),
							          new Style().add((List<Declaration>)rule));
						else if (rule instanceof RuleMargin)
							style.add("@" + ((RuleMargin)rule).getMarginArea(),
							          new Style().add((List<Declaration>)rule));
						else if (rule instanceof RuleVolumeArea)
							style.add("@" + ((RuleVolumeArea)rule).getVolumeArea().value,
							          Style.of((RuleVolumeArea)rule));
						else if (rule instanceof RuleRelativePage)
							style.add(Style.of(((RuleRelativePage)rule).asRulePage()));
						else if (rule instanceof RuleRelativeVolume)
							style.add(Style.of(((RuleRelativeVolume)rule).asRuleVolume()));
						else if (rule instanceof AnyAtRule)
							style.add("@" + ((AnyAtRule)rule).getName(),
							          Style.of((AnyAtRule)rule));
						else
							throw new RuntimeException("coding error");
					}
					try {
						List<XMLStreamWritable<XdmNode>> rules = new ArrayList<>();
						style.toXml(
							propagateException(() -> {
									XMLStreamWritable<XdmNode> w = SaxonHelper.nodeWriter(context.getConfiguration());
									rules.add(w);
									return w.getWriter(); },
								RuntimeException::new),
							deep);
						for (XMLStreamWritable<XdmNode> w : rules)
							result.add(((XdmNode)w.doneWriting().axisIterator(Axis.CHILD).next()).getUnderlyingNode());
					} catch (RuntimeException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				} catch (Exception e) {
					logger.error("Error happened while parsing " + arg, e);
				}
				return new SequenceExtent(result);
			}
		};
	}
	
	private static class Style implements Comparator<String> {
		
		List<Declaration> declarations;
		SortedMap<String,Style> nestedStyles; // sorted by key
		
		public int compare(String selector1, String selector2) {
			if (selector1.startsWith("&") && !selector2.startsWith("&"))
				return 1;
			else if (!selector1.startsWith("&") && selector2.startsWith("&"))
				return -1;
			else
				return selector1.compareTo(selector2);
		}
		
		Style add(Declaration declaration) {
			if (declaration != null) {
				if (this.declarations == null) this.declarations = new ArrayList<Declaration>();
				declarations.add(declaration);
			}
			return this;
		}
		
		Style add(Iterable<Declaration> declarations) {
			if (declarations != null)
				for (Declaration d : declarations)
					add(d);
			return this;
		}
		
		Style add(String selector, Style nestedStyle) {
			if (this.nestedStyles == null) this.nestedStyles = new TreeMap<String,Style>(this);
			if (selector == null)
				add(nestedStyle);
			if (this.nestedStyles.containsKey(selector))
				this.nestedStyles.get(selector).add(nestedStyle);
			else
				this.nestedStyles.put(selector, nestedStyle);
			return this;
		}
		
		Style add(Style style) {
			add(style.declarations);
			if (style.nestedStyles != null)
				for (Map.Entry<String,Style> e : style.nestedStyles.entrySet())
					add(e.getKey(), e.getValue());
			return this;
		}
		
		String toString(String base) {
			StringBuilder b = new StringBuilder();
			StringBuilder rel = new StringBuilder();
			if (declarations != null)
				b.append(serializeDeclarations(declarations));
			if (nestedStyles != null)
				for (Map.Entry<String,Style> e : nestedStyles.entrySet()) {
					if (base != null && e.getKey().startsWith("&")) {
						if (rel.length() > 0) rel.append(" ");
						rel.append(e.getValue().toString(base + e.getKey().substring(1)));
					} else {
						if (b.length() > 0) b.append(" ");
						b.append(e.getValue().toString(e.getKey()));
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
		
		@Override
		public String toString() {
			return toString(null);
		}
		
		void toXml(Supplier<XMLStreamWriter> writers, boolean deep) throws XMLStreamException {
			toXml(writers, deep, false);
		}
		
		void toXml(Supplier<XMLStreamWriter> writers, boolean deep, boolean recursive) throws XMLStreamException {
			if (declarations != null) {
				XMLStreamWriter w = writers.get();
				if (!deep || !recursive || nestedStyles != null)
					writeStartElement(w, CSS_RULE);
				if (!deep)
					writeAttribute(w, STYLE, serializeDeclarations(declarations));
				if (deep) {
					for (Declaration d : declarations) {
						writeStartElement(w, CSS_PROPERTY);
						writeAttribute(w, NAME, d.getProperty());
						writeAttribute(w, VALUE, Strings.join(d, " ", serializeTerm));
						w.writeEndElement();
					}
				}
				if (!deep || !recursive || nestedStyles != null)
					w.writeEndElement();
			}
			if (nestedStyles != null)
				for (Map.Entry<String,Style> e : nestedStyles.entrySet()) {
					XMLStreamWriter w = writers.get();
					writeStartElement(w, CSS_RULE);
					writeAttribute(w, SELECTOR, e.getKey());
					if (!deep)
						writeAttribute(w, STYLE, e.getValue().toString());
					if (deep)
						e.getValue().toXml(() -> w, true, true);
					w.writeEndElement();
				}
		}
		
		static Style of(RulePage page) {
			// assumed to be anonymous page
			Style style = new Style();
			for (Rule<?> r : page)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof RuleMargin)
					style.add("@" + ((RuleMargin)r).getMarginArea(),
					          new Style().add((List<Declaration>)r));
				else
					throw new RuntimeException("coding error");
			String pseudo = page.getPseudo();
			return pseudo == null
				? style
				: new Style().add("&:" + pseudo, style);
		}
		
		static Style of(RuleVolumeArea volumeArea) {
			Style style = new Style();
			for (Rule<?> r : volumeArea)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof RulePage)
					style.add("@page", Style.of((RulePage)r));
				else
					throw new RuntimeException("coding error");
			return style;
		}
		
		static Style of(RuleVolume volume) {
			Style style = new Style();
			for (Rule<?> r : volume)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof RuleVolumeArea)
					style.add("@" + ((RuleVolumeArea)r).getVolumeArea().value, Style.of((RuleVolumeArea)r));
				else
					throw new RuntimeException("coding error");
			String pseudo = volume.getPseudo();
			return pseudo == null
				? style
				: new Style().add("&:" + pseudo, style);
		}
		
		static Style of(AnyAtRule rule) {
			Style style = new Style();
			for (Rule<?> r : rule)
				if (r instanceof Declaration)
					style.add((Declaration)r);
				else if (r instanceof AnyAtRule)
					style.add("@" + ((AnyAtRule)r).getName(), Style.of((AnyAtRule)r));
				else
					throw new RuntimeException("coding error");
			return style;
		}
	}
	
	private static class SelectorPart {
		final Selector.Combinator combinator;
		final Selector.SelectorPart selector;
		SelectorPart (Selector.Combinator combinator, Selector.SelectorPart selector) {
			this.combinator = combinator;
			this.selector = selector;
		}
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			if (combinator != null)
				b.append(combinator.value());
			if (selector instanceof PseudoElementImpl) {
			PseudoElementImpl pe = (PseudoElementImpl)selector;
				b.append(":");
				if (!pe.isSpecifiedAsClass())
					b.append(":");
				b.append(pe.getName());
				String[] args = pe.getArguments();
				if (args.length > 0) {
					b.append("(");
					for (int i = 0; i < args.length; i++) {
						if (i > 0) b.append(", ");
						b.append(args[i]);
					}
					b.append(")");
				}
			} else
				b.append(selector);
			return b.toString();
		}
	}
	
	private static void flattenSelector(List<SelectorPart> collect, Selector.Combinator combinator, Selector.SelectorPart selector) {
		collect.add(new SelectorPart(combinator, selector));
		if (selector instanceof PseudoElementImpl) {
			PseudoElementImpl pe = (PseudoElementImpl)selector;
			if (!pe.getCombinedSelectors().isEmpty())
				for (Selector s: pe.getCombinedSelectors())
					flattenSelector(collect, s);
			else {
				if (!pe.getPseudoClasses().isEmpty())
					for (PseudoClass pc : pe.getPseudoClasses())
						collect.add(new SelectorPart(null, pc));
				if (pe.hasStackedPseudoElement())
					flattenSelector(collect, null, pe.getStackedPseudoElement());
			}
		}
	}
	
	private static void flattenSelector(List<SelectorPart> collect, Selector selector) {
		Selector.Combinator combinator = selector.getCombinator();
		for (Selector.SelectorPart part : selector) {
			flattenSelector(collect, combinator, part);
			combinator = null;
		}
	}
	
	private static List<SelectorPart> flattenSelector(RuleRelativeBlock rule) {
		List<SelectorPart> selector = new ArrayList<>();
		for (Selector s : rule.getSelector())
			flattenSelector(selector, s);
		return selector;
	}
	
	private static String[] serializeSelector(RuleRelativeBlock rule) {
		String head = null;
		StringBuilder b = new StringBuilder();
		for (SelectorPart part : flattenSelector(rule)) {
			if (b.length() == 0)
				b.append("&");
			b.append(part);
			if (head == null) {
				head = b.toString();
				b = new StringBuilder();
			}
		}
		return b.length() == 0
			? new String[]{head}
			: new String[]{head, b.toString()};
	}
	
	private static String serializeDeclarations(List<Declaration> declarations) {
		if (declarations.isEmpty())
			return null;
		List<Declaration> sortedDeclarations = new ArrayList<Declaration>(declarations);
		Collections.sort(sortedDeclarations);
		return Strings.join(sortedDeclarations, " ", serializeDeclaration);
	}
	
	private static String serializeDeclaration(Declaration declaration) {
		String value = "";
		for (Term<?> t : declaration) {
			if (!value.isEmpty()) {
				Term.Operator o = t.getOperator();
				if (o != null)
					switch (o) {
					case COMMA:
						value += ","; }
				value += " "; }
			value += serializeTerm.apply(t); }
		return declaration.getProperty() + ": " + value + ";";
	}
	
	private static Function<Object,String> serializeDeclaration = new Function<Object,String>() {
		public String apply(Object declaration) {
			if (declaration instanceof String) // separator
				return (String)declaration;
			if (declaration instanceof Declaration)
				return serializeDeclaration((Declaration)declaration);
			else
				throw new IllegalArgumentException("Coding error");
		}
	};
	
	private static Function<Object,String> serializeTerm = new Function<Object,String>() {
		public String apply(Object term) {
			if (term instanceof TermInteger) {
				TermInteger integer = (TermInteger)term;
				return "" + integer.getIntValue(); }
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
					s += serializeTerm.apply(t); }
				return s; }
			else if (term instanceof TermPair) {
				TermPair<?,?> pair = (TermPair<?,?>)term;
				Object val = pair.getValue();
				return "" + pair.getKey() + " " + (val instanceof Term ? serializeTerm.apply((Term)val) : val.toString()); }
			else if (term instanceof TermString) {
				TermString string = (TermString)term;
				return "'" + string.getValue().replaceAll("\n", "\\\\A") + "'"; }
			else
				return term.toString().replaceAll("^[,/ ]+", "");
		}
	};
	
	private static final Logger logger = LoggerFactory.getLogger(ParseStylesheetDefinition.class);
	
}
