package org.daisy.braille.css;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;

import org.daisy.braille.css.BrailleCSSProperty.AbsoluteMargin;
import org.daisy.braille.css.BrailleCSSProperty.BorderAlign;
import org.daisy.braille.css.BrailleCSSProperty.BorderPattern;
import org.daisy.braille.css.BrailleCSSProperty.BorderStyle;
import org.daisy.braille.css.BrailleCSSProperty.BorderWidth;
import org.daisy.braille.css.BrailleCSSProperty.BrailleCharset;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.Flow;
import org.daisy.braille.css.BrailleCSSProperty.GenericVendorCSSPropertyProxy;
import org.daisy.braille.css.BrailleCSSProperty.HyphenateCharacter;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.LetterSpacing;
import org.daisy.braille.css.BrailleCSSProperty.LineHeight;
import org.daisy.braille.css.BrailleCSSProperty.ListStyleType;
import org.daisy.braille.css.BrailleCSSProperty.Margin;
import org.daisy.braille.css.BrailleCSSProperty.MaxHeight;
import org.daisy.braille.css.BrailleCSSProperty.MaxLength;
import org.daisy.braille.css.BrailleCSSProperty.MinLength;
import org.daisy.braille.css.BrailleCSSProperty.Padding;
import org.daisy.braille.css.BrailleCSSProperty.Page;
import org.daisy.braille.css.BrailleCSSProperty.RenderTableBy;
import org.daisy.braille.css.BrailleCSSProperty.Size;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.BrailleCSSProperty.TableHeaderPolicy;
import org.daisy.braille.css.BrailleCSSProperty.TextIndent;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreak;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreakInside;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermNumber;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.Repeater;
import cz.vutbr.web.domassign.Variator;

public class BrailleCSSDeclarationTransformer extends DeclarationTransformer {
	
	public BrailleCSSDeclarationTransformer() {
		this(new SupportedBrailleCSS());
	}
	
	public BrailleCSSDeclarationTransformer(SupportedCSS css) {
		super(css);
	}
	
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
					log.debug("Unable to find method for property {}.", property);
				}
			}
		}
		log.debug("Totally found {} parsing methods", map.size());
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
						return genericProperty(GenericVendorCSSPropertyProxy.class, (TermIdent)term,
						                       true, properties, property);
					else if (term instanceof TermInteger)
						return genericTerm(TermInteger.class, term, d.getProperty(),
						                   GenericVendorCSSPropertyProxy.valueOf(null), false, properties, values);
					else if (term instanceof TermFunction)
						return genericTerm(TermFunction.class, term, d.getProperty(),
						                   GenericVendorCSSPropertyProxy.valueOf(null), false, properties, values);
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
	private boolean processBorder(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Variator border = new BorderVariator();
		border.assignTermsFromDeclaration(d);
		border.assignDefaults(properties, values);
		return border.vary(properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderAlign(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Repeater r = new BorderAlignRepeater();
		return r.repeatOverFourTermDeclaration(d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderBottom(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Variator borderSide = new BorderSideVariator("bottom");
		borderSide.assignTermsFromDeclaration(d);
		borderSide.assignDefaults(properties, values);
		return borderSide.vary(properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderBottomAlign(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderAlign.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processBorderBottomPattern(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(BorderPattern.class, BorderPattern.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderBottomStyle(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderStyle.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderBottomWidth(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(BorderWidth.class, BorderWidth.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderLeft(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Variator borderSide = new BorderSideVariator("left");
		borderSide.assignTermsFromDeclaration(d);
		borderSide.assignDefaults(properties, values);
		return borderSide.vary(properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderLeftAlign(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderAlign.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processBorderLeftPattern(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(BorderPattern.class, BorderPattern.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderLeftStyle(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderStyle.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderLeftWidth(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(BorderWidth.class, BorderWidth.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderRight(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Variator borderSide = new BorderSideVariator("right");
		borderSide.assignTermsFromDeclaration(d);
		borderSide.assignDefaults(properties, values);
		return borderSide.vary(properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderRightAlign(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderAlign.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processBorderRightPattern(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(BorderPattern.class, BorderPattern.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderRightStyle(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderStyle.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderRightWidth(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(BorderWidth.class, BorderWidth.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderStyle(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Repeater r = new BorderStyleRepeater();
		return r.repeatOverFourTermDeclaration(d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderTop(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Variator borderSide = new BorderSideVariator("top");
		borderSide.assignTermsFromDeclaration(d);
		borderSide.assignDefaults(properties, values);
		return borderSide.vary(properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderTopAlign(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderAlign.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processBorderTopPattern(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrDotPattern(BorderPattern.class, BorderPattern.dot_pattern,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderTopStyle(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BorderStyle.class, d, properties);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderTopWidth(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(BorderWidth.class, BorderWidth.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBorderWidth(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		Repeater r = new BorderWidthRepeater();
		return r.repeatOverFourTermDeclaration(d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processBrailleCharset(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdent(BrailleCharset.class, d, properties);
	}
	
	private final static Set<String> validContentFuncNames
	= new HashSet<String>(Arrays.asList("content", "attr", "counter", "counters", "string", "leader", "flow",
	                                    "target-text", "target-string", "target-counter", "target-content"));
	
	private final static Pattern customContentFuncName = Pattern.compile("^-.*"); // is the rest handled in ANTLR?
	
	@SuppressWarnings("unused")
	protected boolean processContent(Declaration d,
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

	private static final Pattern HYPHENATE_CHARACTER_RE = Pattern.compile("[\u2800-\u28ff]+");

	@SuppressWarnings("unused")
	private boolean processHyphenateCharacter(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (d.size() != 1)
			return false;
		Term<?> term = d.get(0);
		String prop = d.getProperty();
		if (genericTermIdent(HyphenateCharacter.class, term, ALLOW_INH, prop, properties))
			return true;
		else if (TermString.class.isInstance(term)) {
			if (!HYPHENATE_CHARACTER_RE.matcher("" + term.getValue()).matches()) {
				return false;
			}
			properties.put(prop, HyphenateCharacter.braille_string);
			values.put(prop, term);
			return true; }
		return false;
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
		if (d.size() != 1)
			return false;
		Term<?> term = d.get(0);
		return genericTermIdent(LineHeight.class, term, ALLOW_INH, d.getProperty(),
				properties)
			|| genericTerm(TermInteger.class, term, d.getProperty(), LineHeight.number,
				true, properties, values)
			|| genericTerm(TermNumber.class, term, d.getProperty(), LineHeight.number,
				true, properties, values)
			|| genericTerm(TermPercent.class, term, d.getProperty(), LineHeight.percentage,
				true, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processListStyle(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return processListStyleType(d, properties, values);
	}
	
	private boolean processListStyleType(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		String propertyName = "list-style-type";
		if (d.size() != 1)
			return false;
		Term<?> term = d.get(0);
		if (genericTermIdent(ListStyleType.class, term, ALLOW_INH, propertyName, properties))
			return true;
		else
			try {
				if (term instanceof TermIdent) {
					properties.put(propertyName, ListStyleType.counter_style_name);
					values.put(propertyName, term);
					return true; }
				else if (TermString.class.isInstance(term)) {
					properties.put(propertyName, ListStyleType.braille_string);
					values.put(propertyName, term);
					return true; }
				else if (TermFunction.class.isInstance(term)
				         && "symbols".equals(((TermFunction)term).getFunctionName().toLowerCase())) {
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
	private boolean processMaxHeight(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(MaxHeight.class, MaxHeight.integer, false,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processMaxLength(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(MaxLength.class, MaxLength.integer, true,
				d, properties, values);
	}
	
	@SuppressWarnings("unused")
	private boolean processMinLength(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(MinLength.class, MinLength.integer, true,
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
	private boolean processSize(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (d.size() == 1 && genericOneIdent(StringSet.class, d, properties))
			return true;
		TermInteger width = null;
		TermInteger height = null;
		for (Term<?> t : d.asList()) {
			if (height != null || !(t instanceof TermInteger)) {
				return false;
			} else if (width == null) {
				width = (TermInteger)t;
			} else {
				height = (TermInteger)t;
			}
		}
		if (height == null) {
			return false;
		}
		TermList size = tf.createList(2);
		size.add(width);
		size.add(height);
		properties.put("size", Size.integer_pair);
		values.put("size", size);
		return true;
	}
	
	@SuppressWarnings("unused")
	protected boolean processStringSet(Declaration d,
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
	
	protected boolean processTextTransform(Declaration d,
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
	
	private final class MarginRepeater extends Repeater {

		public MarginRepeater() {
			super(4, css);
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
			super(4, css);
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
	
	private final class BorderStyleRepeater extends Repeater {
		
		public BorderStyleRepeater() {
			super(4, css);
			this.type = BorderStyle.class;
			names.add("border-top-style");
			names.add("border-right-style");
			names.add("border-bottom-style");
			names.add("border-left-style");
		}
		
		@Override
		protected boolean operation(int i, Map<String, CSSProperty> properties,
		                            Map<String, Term<?>> values) {
			return genericTermIdent(BorderStyle.class, terms.get(i), ALLOW_INH, names.get(i),
			                        properties);
		}
	}
	
	private final class BorderAlignRepeater extends Repeater {
		
		public BorderAlignRepeater() {
			super(4, css);
			this.type = BorderAlign.class;
			names.add("border-top-align");
			names.add("border-right-align");
			names.add("border-bottom-align");
			names.add("border-left-align");
		}
		
		@Override
		protected boolean operation(int i, Map<String, CSSProperty> properties,
		                            Map<String, Term<?>> values) {
			return genericTermIdent(BorderAlign.class, terms.get(i), ALLOW_INH, names.get(i),
			                        properties);
		}
	}
	
	private final class BorderWidthRepeater extends Repeater {
		
		public BorderWidthRepeater() {
			super(4, css);
			this.type = BorderWidth.class;
			names.add("border-top-width");
			names.add("border-right-width");
			names.add("border-bottom-width");
			names.add("border-left-width");
		}

		@Override
		protected boolean operation(int i, Map<String, CSSProperty> properties,
		                            Map<String, Term<?>> values) {
			return genericTermIdent(type, terms.get(i), ALLOW_INH, names.get(i), properties)
				|| genericTerm(TermInteger.class, terms.get(i), names.get(i), BorderWidth.integer,
				               false, properties, values);
		}
	}
	
	
	/****************************************************************
	 * VARIATOR CLASSES
	 ****************************************************************/
	
	private final class BorderVariator extends Variator {
		
		public static final int WIDTH = 0;
		public static final int STYLE = 1;
		public static final int ALIGN = 2;
		
		private List<Repeater> repeaters;
		
		public BorderVariator() {
			super(3, css);
			types.add(BorderWidth.class);
			types.add(BorderStyle.class);
			types.add(BorderAlign.class);
			repeaters = new ArrayList<Repeater>(variants);
			repeaters.add(new BorderWidthRepeater());
			repeaters.add(new BorderStyleRepeater());
			repeaters.add(new BorderAlignRepeater());
		}
		
		@Override
		protected boolean variant(int variant, IntegerRef iteration,
		                          Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
			int i = iteration.get();
			Term<?> term = terms.get(i);
			Repeater r;
			switch (variant) {
			case WIDTH:
			case STYLE:
			case ALIGN:
				r = repeaters.get(variant);
				r.assignTerms(term, term, term, term);
				return r.repeat(properties, values);
			default:
				return false;
			}
		}
		
		@Override
		protected boolean checkInherit(int variant, Term<?> term, Map<String, CSSProperty> properties) {
			if (!(term instanceof TermIdent)
			    || !CSSProperty.INHERIT_KEYWORD.equalsIgnoreCase(((TermIdent) term).getValue())) {
				return false;
			}
			if (variant == ALL_VARIANTS) {
				for (int i = 0; i < variants; i++) {
					Repeater r = repeaters.get(i);
					r.assignTerms(term, term, term, term);
					r.repeat(properties, null);
				}
				return true;
			}
			Repeater r = repeaters.get(variant);
			r.assignTerms(term, term, term, term);
			r.repeat(properties, null);
			return true;
		}
		
		@Override
		public void assignDefaults(Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
			for (Repeater r : repeaters)
				r.assignDefaults(properties, values);
		}
	}
	
	private final class BorderSideVariator extends Variator {
		
		public static final int ALIGN = 0;
		public static final int STYLE = 1;
		public static final int WIDTH = 2;
		
		private final String borderPatternName;
		
		public BorderSideVariator(String side) {
			super(3, css);
			names.add("border-" + side + "-align");
			types.add(BorderAlign.class);
			names.add("border-" + side + "-style");
			types.add(BorderStyle.class);
			names.add("border-" + side + "-width");
			types.add(BorderWidth.class);
			borderPatternName = "border-" + side + "-pattern";
		}
		
		@Override
		public void assignDefaults(Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
			super.assignDefaults(properties, values);
			assignDefault(borderPatternName, properties, values);
		}
		
		@Override
		protected boolean variant(int variant, IntegerRef iteration,
		                          Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
			int i = iteration.get();
			switch (variant) {
			case WIDTH:
				return genericTermIdent(types.get(variant), terms.get(i), AVOID_INH, names.get(variant),
				                        properties)
					|| genericTerm(TermInteger.class, terms.get(i), names.get(variant), BorderWidth.integer,
					               false, properties, values);
			case ALIGN:
			case STYLE:
				return genericTermIdent(types.get(variant), terms.get(i), AVOID_INH, names.get(variant),
				                        properties);
			default:
				return false;
			}
		}
		
		private boolean patternVariant(Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
			if (terms.size() != 1)
				return false;
			Term<?> t = terms.get(0);
			if (genericTermIdent(BorderPattern.class, t, ALLOW_INH, borderPatternName, properties))
				return true;
			try {
				if (TermIdent.class.isInstance(t)) {
					TermDotPattern value = TermDotPattern.createDotPattern((TermIdent)t);
					properties.put(borderPatternName, BorderPattern.dot_pattern);
					values.put(borderPatternName, value);
					return true;
				}
			} catch (Exception e) {
			}
			return false;
		}
		
		@Override
		public boolean vary(Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
			boolean rv = false;
			if (super.vary(properties, values))
				rv = true;
			if (patternVariant(properties, values))
				rv = true;
			return rv;
		}
	}
	
	private final void assignDefault(String propertyName, Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		CSSProperty dp = css.getDefaultProperty(propertyName);
		if (dp != null)
			properties.put(propertyName, dp);
		Term<?> dv = css.getDefaultValue(propertyName);
		if (dv != null)
			values.put(propertyName, dv);
	}
}
