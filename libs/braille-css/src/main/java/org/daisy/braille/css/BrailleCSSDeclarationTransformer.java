package org.daisy.braille.css;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;

import org.daisy.braille.css.BrailleCSSProperty.AbsoluteMargin;
import org.daisy.braille.css.BrailleCSSProperty.Border;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.Flow;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.LetterSpacing;
import org.daisy.braille.css.BrailleCSSProperty.LineHeight;
import org.daisy.braille.css.BrailleCSSProperty.ListStyleType;
import org.daisy.braille.css.BrailleCSSProperty.Margin;
import org.daisy.braille.css.BrailleCSSProperty.MaxLength;
import org.daisy.braille.css.BrailleCSSProperty.MinLength;
import org.daisy.braille.css.BrailleCSSProperty.Padding;
import org.daisy.braille.css.BrailleCSSProperty.Page;
import org.daisy.braille.css.BrailleCSSProperty.RenderTableBy;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.BrailleCSSProperty.TableHeaderPolicy;
import org.daisy.braille.css.BrailleCSSProperty.TextIndent;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreak;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreakInside;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.GenericCSSPropertyProxy;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.Repeater;

public class BrailleCSSDeclarationTransformer extends DeclarationTransformer {
	
	protected Map<String, Method> parsingMethods() {
		Map<String, Method> map = new HashMap<String, Method>(css.getTotalProperties(), 1.0f);
		for (String property : css.getDefinedPropertyNames()) {
			try {
				Method m = BrailleCSSDeclarationTransformer.class.getDeclaredMethod(
					camelCase("process-" + property),
					Declaration.class, Map.class, Map.class);
				map.put(property, m);
			} catch (Exception e) {
				try {
					Method m = DeclarationTransformer.class.getDeclaredMethod(
						DeclarationTransformer.camelCase("process-" + property),
						Declaration.class, Map.class, Map.class);
					map.put(property, m);
				} catch (Exception e2) {
					log.warn("Unable to find method for property {}.", property);
				}
			}
		}
		log.info("Totally found {} parsing methods", map.size());
		return map;
	}
	
	@Override
	public boolean parseDeclaration(Declaration d, Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		String property = d.getProperty().toLowerCase();
		if (!css.isSupportedCSSProperty(property)) {
			if (property.startsWith("-")) {
				// vendor extension
				if (d.size() == 1) {
					Term<?> term = d.get(0);
					if (term instanceof TermIdent)
						return genericProperty(GenericCSSPropertyProxy.class, (TermIdent)term,
						                       true, properties, property);
					else if (term instanceof TermInteger)
						return genericTerm(TermInteger.class, term, d.getProperty(),
						                   null, false, properties, values);
					else if (term instanceof TermFunction)
						return genericTerm(TermFunction.class, term, d.getProperty(),
						                   null, false, properties, values);
				}
				log.warn("Ignoring unsupported declaration: " + declarationToString(d));
			} else {
				log.debug("Ignoring unsupported property: " + property);
			}
		} else {
			try {
				Method m = methods.get(property);
				if (m != null)
					try {
						return (Boolean)m.invoke(this, d, properties, values);
					} catch (IllegalAccessException e) {
						if (super.parseDeclaration(d, properties, values))
							return true;
					} catch (IllegalArgumentException e) {
						if (super.parseDeclaration(d, properties, values))
							return true;
					} catch (InvocationTargetException e) {
					}
			} catch (Exception e) {
			}
			log.warn("Ignoring unsupported declaration: " + declarationToString(d));
		}
		return false;
	}
	
	private static String declarationToString(Declaration d) {
		StringBuilder b = new StringBuilder();
		b.append(d.getProperty()).append(":");
		for (Term<?> t : d)
			b.append(" ").append(t);
		b.append(";");
		return b.toString();
	}
	
	/****************************************************************
	 * PROCESSING METHODS
	 ****************************************************************/
	
	@SuppressWarnings("unused")
	private boolean processBorderBottom(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(Border.class, Border.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderLeft(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(Border.class, Border.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderRight(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(Border.class, Border.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderTop(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(Border.class, Border.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorder(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Repeater r = new BorderRepeater();
		return r.repeatOverFourTermDeclaration(d, properties, values);
	}
	
	private final static Set<String> validContentFuncNames
	= new HashSet<String>(Arrays.asList("content", "attr", "counter", "string", "leader", "flow",
	                                    "target-text", "target-string", "target-counter", "target-content"));
	
	private final static Pattern customContentFuncName = Pattern.compile("^-.*"); // is the rest handled in ANTLR?
	
	@SuppressWarnings("unused")
	private boolean processContent(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {

		if (d.size() == 1 && genericOneIdent(Content.class, d, properties))
			return true;
		
		TermList list = tf.createList();
		for (Term<?> t : d.asList()) {
			if (t instanceof TermString)
				list.add(t);
			else if (t instanceof TermFunction) {
				String funcName = ((TermFunction)t).getFunctionName();
				if (validContentFuncNames.contains(funcName.toLowerCase())
				    || customContentFuncName.matcher(funcName).matches())
					list.add(t);
				else
					return false; }
			else
				return false;
		}
		if (list.isEmpty())
			return false;

		properties.put("content", Content.content_list);
		values.put("content", list);
		return true;
	}
	
	private final static Pattern customDisplayIdent = Pattern.compile("^-.*"); // is the rest handled in ANTLR?
	
	@SuppressWarnings("unused")
	private boolean processDisplay(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (d.size() != 1)
			return false;
		Term t = d.get(0);
		String prop = d.getProperty();
		if (genericTermIdent(Display.class, t, ALLOW_INH, prop, properties))
			return true;
		if (t instanceof TermIdent) {
			if (customDisplayIdent.matcher(((TermIdent)t).getValue()).matches()) {
				properties.put(prop, Display.custom);
				values.put(prop, t);
				return true; }}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean processFlow(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrIdentifier(Flow.class, Flow.identifier, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processHyphens(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(Hyphens.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processLeft(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processLetterSpacing(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (genericOneIdentOrInteger(LetterSpacing.class, LetterSpacing.length, true,
				d, properties, values))
			return true;
		else {
			log.warn("{} not supported, illegal number", d);
			return false; }
	}
	
	@SuppressWarnings("unused")
	private boolean processLineHeight(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(LineHeight.class, LineHeight.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processListStyleType(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (d.size() != 1)
			return false;
		Term<?> term = d.get(0);
		if (genericTermIdent(ListStyleType.class, term, ALLOW_INH, d.getProperty(), properties))
			return true;
		else
			try {
				if (TermString.class.isInstance(term)) {
					String propertyName = d.getProperty();
					properties.put(propertyName, ListStyleType.braille_string);
					values.put(propertyName, term);
					return true; }
				else if (TermFunction.class.isInstance(term)
				         && "symbols".equals(((TermFunction)term).getFunctionName().toLowerCase())) {
					String propertyName = d.getProperty();
					properties.put(propertyName, ListStyleType.symbols_fn);
					values.put(propertyName, term);
					return true; }}
			catch (Exception e) {}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean processMarginBottom(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Margin.class, Margin.integer, true,
				d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processMarginLeft(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Margin.class, Margin.integer, false,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processMarginRight(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Margin.class, Margin.integer, false,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processMarginTop(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Margin.class, Margin.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processMargin(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Repeater r = new MarginRepeater();
		return r.repeatOverFourTermDeclaration(d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processMaxLength(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(MaxLength.class, MaxLength.integer, false,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processMinLength(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(MinLength.class, MinLength.integer, false,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processPaddingBottom(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Padding.class, Padding.integer, true,
				d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processPaddingLeft(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Padding.class, Padding.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processPaddingRight(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Padding.class, Padding.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processPaddingTop(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Padding.class, Padding.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processPadding(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Repeater r = new PaddingRepeater();
		return r.repeatOverFourTermDeclaration(d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processPage(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrIdentifier(Page.class, Page.identifier, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processRenderTableBy(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		
		if (d.size() == 1 && genericOneIdent(RenderTableBy.class, d, properties))
			return true;
		TermList list = tf.createList();
		for (Term<?> t : d.asList())
			if (t instanceof TermIdent)
				list.add(t);
			else
				return false;
		if (list.isEmpty())
			return false;
		properties.put("render-table-by", RenderTableBy.axes);
		values.put("render-table-by", list);
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean processRight(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processStringSet(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		
		if (d.size() == 1 && genericOneIdent(StringSet.class, d, properties))
			return true;
		
		final Set<String> validFuncNames = new HashSet<String>(Arrays.asList("content", "attr"));
		
		TermList list = tf.createList();
		TermList contentList = tf.createList();
		String stringName = null;
		boolean first = true;
		for (Term<?> t : d.asList()) {
			if (stringName == null) {
				if (t instanceof TermIdent)
					stringName = ((TermIdent)t).getValue();
				else
					return false;
			} else if (t instanceof TermIdent) {
				if (contentList.isEmpty())
					return false;
				TermPair pair = tf.createPair(stringName, contentList);
				if (!first) pair.setOperator(Term.Operator.COMMA);
				list.add(pair);
				stringName = ((TermIdent)t).getValue();
				contentList = tf.createList();
				first = false;
			} else if (t instanceof TermString)
				contentList.add(t);
			else if (t instanceof TermFunction
			         && validFuncNames.contains(((TermFunction)t).getFunctionName().toLowerCase()))
				contentList.add(t);
			else
				return false;
		}
		
		if (contentList.isEmpty())
			return false;
		TermPair pair = tf.createPair(stringName, contentList);
		if (!first) pair.setOperator(Term.Operator.COMMA);
		list.add(pair);
		
		properties.put("string-set", StringSet.list_values);
		values.put("string-set", list);
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean processTableHeaderPolicy(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(TableHeaderPolicy.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processTextIndent(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(TextIndent.class, TextIndent.integer, false,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processTextTransform(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		
		if (d.size() == 1 && genericOneIdent(TextTransform.class, d, properties))
			return true;
		
		TermList list = tf.createList();
		for (Term<?> t : d.asList()) {
			if (t instanceof TermIdent) {
				String value = ((TermIdent)t).getValue().toLowerCase();
				if (!value.equals("auto"))
					list.add(t);
			}
			else
				return false;
		}
		
		if (list.isEmpty())
			return false;
		
		properties.put("text-transform", TextTransform.list_values);
		values.put("text-transform", list);
		return true;
	}
	
	@SuppressWarnings("unused")
	private boolean processVolumeBreakAfter(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(VolumeBreak.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processVolumeBreakBefore(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(VolumeBreak.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processVolumeBreakInside(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (genericOneIdent(VolumeBreakInside.class, d, properties))
			return true;
		if (d.size() == 1) {
			Term<?> term = d.get(0);
			if (term instanceof TermFunction) {
				TermFunction fun = (TermFunction)term;
				if ("-obfl-keep".equals(fun.getFunctionName())
					&& fun.size() == 1
					&& fun.get(0) instanceof TermInteger) {
					properties.put("volume-break-inside", VolumeBreakInside.obfl_keep);
					values.put("volume-break-inside", fun);
					return true;
				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean processWhiteSpace(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(WhiteSpace.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processWordSpacing(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (genericOneIdentOrInteger(WordSpacing.class, WordSpacing.length, true,
				d, properties, values))
			return true;
		else {
			log.warn("{} not supported, illegal number", d);
			return false; }
	}
	
	/****************************************************************
	 * GENERIC METHODS
	 ****************************************************************/
	
	private <T extends CSSProperty> boolean genericOneIdentOrDotPattern(
			Class<T> type, T dotPatternIdentification, Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {

		if (d.size() != 1)
			return false;
		
		Term<?> term = d.get(0);
		
		if (genericTermIdent(type, term, ALLOW_INH, d.getProperty(),
				properties))
			return true;
		
		try {
			if (TermIdent.class.isInstance(term)) {
				String propertyName = d.getProperty();
				TermDotPattern value = TermDotPattern.createDotPattern((TermIdent)term);
				properties.put(propertyName, dotPatternIdentification);
				values.put(propertyName, value);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	private <T extends CSSProperty> boolean genericOneIdentOrIdentifier(
			Class<T> type, T identifierIdentification, boolean sanify,
			Declaration d, Map<String, CSSProperty> properties,
			Map<String, Term<?>> values) {

		if (d.size() != 1)
			return false;

		return genericTermIdent(type, d.get(0), ALLOW_INH, d.getProperty(),
				properties)
				|| genericTerm(TermIdent.class, d.get(0), d.getProperty(),
						identifierIdentification, sanify, properties, values);
	}
	
	
	/****************************************************************
	 * REPEATER CLASSES
	 ****************************************************************/
	
	private final class BorderRepeater extends Repeater {
			
		public BorderRepeater() {
			super(4);
			type = Border.class;
			names.add("border-top");
			names.add("border-right");
			names.add("border-bottom");
			names.add("border-left");
		}
		
		protected boolean operation(int i,
		                            Map<String,CSSProperty> properties,
		                            Map<String,Term<?>> values) {
			
			Term<?> term = terms.get(i);
			
			if (genericTermIdent(type, term, AVOID_INH, names.get(i), properties))
				return true;
			
			try {
				if (TermIdent.class.isInstance(term)) {
					String propertyName = names.get(i);
					TermDotPattern value = TermDotPattern.createDotPattern((TermIdent)term);
					properties.put(propertyName, Border.dot_pattern);
					values.put(propertyName, value);
					return true;
				}
			} catch (Exception e) {
			}
			return false;
		}
	}
	
	private final class MarginRepeater extends Repeater {

		public MarginRepeater() {
			super(4);
			type = Margin.class;
			names.add("margin-top");
			names.add("margin-right");
			names.add("margin-bottom");
			names.add("margin-left");
		}
		
		protected boolean operation(int i,
		                            Map<String,CSSProperty> properties,
		                            Map<String,Term<?>> values) {
			return genericTermIdent(type, terms.get(i), AVOID_INH, names.get(i), properties)
				|| genericTerm(TermInteger.class, terms.get(i), names.get(i),
				               Margin.integer, false, properties, values);
		}
	}
	
	private final class PaddingRepeater extends Repeater {
			
		public PaddingRepeater() {
			super(4);
			type = Padding.class;
			names.add("padding-top");
			names.add("padding-right");
			names.add("padding-bottom");
			names.add("padding-left");
		}
		
		protected boolean operation(int i,
		                            Map<String,CSSProperty> properties,
		                            Map<String,Term<?>> values) {
			return genericTermIdent(type, terms.get(i), AVOID_INH, names.get(i), properties)
				|| genericTerm(TermInteger.class, terms.get(i), names.get(i),
				               Padding.integer, false, properties, values);
		}
	}
}
