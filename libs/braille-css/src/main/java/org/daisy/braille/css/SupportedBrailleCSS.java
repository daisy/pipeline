package org.daisy.braille.css;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.Set;

import com.google.common.collect.ForwardingMap;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.Border;
import cz.vutbr.web.css.CSSProperty.CounterIncrement;
import cz.vutbr.web.css.CSSProperty.CounterReset;
import cz.vutbr.web.css.CSSProperty.CounterSet;
import cz.vutbr.web.css.CSSProperty.PageBreak;
import cz.vutbr.web.css.CSSProperty.PageBreakInside;
import cz.vutbr.web.css.CSSProperty.TextAlign;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;
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
import org.daisy.braille.css.BrailleCSSProperty.Orphans;
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
import org.daisy.braille.css.BrailleCSSProperty.Widows;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bert
 */
public class SupportedBrailleCSS extends DeclarationTransformer implements SupportedCSS {

	public SupportedBrailleCSS() {
		this(false, true);
	}

	public SupportedBrailleCSS(boolean allowComponentProperties, boolean allowShorthandProperties) {
		this(allowComponentProperties, allowShorthandProperties, Collections.emptyList(), true);
	}

	/**
	 * @param allowUnknownVendorExtensions Whether to allow unknown vendor extensions.
	 */
	public SupportedBrailleCSS(boolean allowComponentProperties,
	                           boolean allowShorthandProperties,
	                           Collection<BrailleCSSExtension> extensions,
	                           boolean allowUnknownVendorExtensions) {
		// SupportedCSSImpl is defined at the bottom of this file
		// it is a separate class because we need an argument to pass to the constructor of DeclarationTransformer
		super(new SupportedCSSImpl(allowComponentProperties, allowShorthandProperties, extensions));
		this.methods = parsingMethods(extensions);
		this.extensions = new ArrayList<>();
		for (BrailleCSSExtension x : extensions)
			if (x.getPrefix() == null || !x.getPrefix().matches("-.+-"))
				log.warn("CSS extension without prefix ignored: " + x);
			else
				this.extensions.add(x);
		this.allowUnknownVendorExtensions = allowUnknownVendorExtensions;
	}

	protected SupportedBrailleCSS(SupportedCSS css) {
		super(css);
		this.extensions = null;
		this.allowUnknownVendorExtensions = false;
	}

	protected final Collection<BrailleCSSExtension> extensions;
	protected final boolean allowUnknownVendorExtensions;

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	@Override
	protected final Map<String, Method> parsingMethods() {
		return null;
	}

	private Map<String, Method> parsingMethods(Collection<BrailleCSSExtension> extensions) {
		Map<String, Method> map = new HashMap<String, Method>(css.getTotalProperties(), 1.0f);
		for (String property : css.getDefinedPropertyNames()) {
			boolean isExtensionProperty = false; {
				for (BrailleCSSExtension x : extensions)
					if (property.startsWith(x.getPrefix())) {
						isExtensionProperty = true;
						break; }}
			if (isExtensionProperty)
				continue; // will be handled by extension
			try {
				Method m = SupportedBrailleCSS.class.getDeclaredMethod(
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
		if (methods == null || extensions == null)
			throw new IllegalStateException("parseDeclaration() method must be overridden");
		String property = d.getProperty().toLowerCase();
		if (property.startsWith("-")) {
			for (BrailleCSSExtension x : extensions)
				if (property.startsWith(x.getPrefix())) {
					if (!x.isSupportedCSSProperty(property)) {
						log.debug("Ignoring unsupported property: " + property);
						return false;
					} else if (x.parseDeclaration(d, properties, values)) {
						return true;
					} else {
						log.warn("Ignoring unsupported declaration: " + declarationToString(d));
						return false;
					}
				}
			if (!allowUnknownVendorExtensions) {
				log.debug("Ignoring unsupported property: " + property);
				return false;
			}
			if (d.size() == 1) {
				Term<?> term = d.get(0);
				if (term instanceof TermIdent)
					return genericProperty(GenericVendorCSSPropertyProxy.class, (TermIdent)term,
					                       ALLOW_INH, properties, property);
				else if (term instanceof TermInteger)
					return genericTerm(TermInteger.class, term, d.getProperty(),
					                   GenericVendorCSSPropertyProxy.valueOf(null), false, properties, values);
				else if (term instanceof TermFunction)
					return genericTerm(TermFunction.class, term, d.getProperty(),
					                   GenericVendorCSSPropertyProxy.valueOf(null), false, properties, values);
			}
			log.warn("Ignoring unsupported declaration: " + declarationToString(d));
			return false;
		}
		if (css.isSupportedCSSProperty(property)) {
			try {
				Method m = methods.get(property);
				if (m != null)
					try {
						if ((Boolean)m.invoke(this, d, properties, values))
							return true;
					} catch (IllegalAccessException e) {
						if (super.parseDeclaration(d, properties, values))
							return true;
					} catch (IllegalArgumentException e) {
						if (super.parseDeclaration(d, properties, values))
							return true;
					} catch (InvocationTargetException e) {
					}
				// might be a declaration with a non-standard value that an extension can parse
				for (BrailleCSSExtension x : extensions)
					if (x.parseDeclaration(d, properties, values))
						return true;
			} catch (Exception e) {
			}
		} else {
			// might be an unprefixed extension property (which the extension's parser may or may not support)
			for (BrailleCSSExtension x : extensions)
				if (x.isSupportedCSSProperty(x.getPrefix() + property))
					if (x.parseDeclaration(d,
					                       new NormalizingMap<CSSProperty>(x, properties),
					                       new NormalizingMap<Term<?>>(x, values)))
						return true;
		}
		log.warn("Ignoring unsupported declaration: " + declarationToString(d));
		return false;
	}

	/**
	 * Normalize property names by adding a prefix if needed. No warnings are issued.
	 */
	private class NormalizingMap<T> extends ForwardingMap<String,T> {

		private final BrailleCSSExtension extension;
		private final Map<String,T> map;

		public NormalizingMap(BrailleCSSExtension extension, Map<String,T> map) {
			this.extension = extension;
			this.map = map;
		}

		@Override
		protected Map<String,T> delegate() {
			return map;
		}

		@Override
		public T put(String propertyName, T value) {
			if (!extension.isSupportedCSSProperty(propertyName)
			    && !propertyName.startsWith(extension.getPrefix())
			    && extension.isSupportedCSSProperty(extension.getPrefix() + propertyName))
				propertyName = extension.getPrefix() + propertyName;
			return super.put(propertyName, value);
		}
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

	private final static Pattern customIdentOrFuncName = Pattern.compile("^-.*");

	@SuppressWarnings("unused")
	protected boolean processContent(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (d.size() == 1 && genericOneIdent(Content.class, d, properties))
			return true;
		TermList list = tf.createList();
		for (Term<?> t : d.asList()) {
			if (t instanceof TermString) {
				list.add(t);
				continue;
			} else if (t instanceof TermFunction) {
				String funcName = ((TermFunction)t).getFunctionName();
				if (validContentFuncNames.contains(funcName.toLowerCase())) {
					list.add(t);
					continue;
				}
			}
			boolean parsedByExtension = false;
			for (BrailleCSSExtension x : extensions) {
				if (x.parseContentTerm(t, list)) {
					parsedByExtension = true;
					break;
				}
			}
			if (parsedByExtension)
				continue;
			if (allowUnknownVendorExtensions)
				if (t instanceof TermFunction) {
					String funcName = ((TermFunction)t).getFunctionName();
					if (customIdentOrFuncName.matcher(funcName).matches()) {
						for (BrailleCSSExtension x : extensions)
							if (funcName.startsWith(x.getPrefix()))
								return false; // x.parseContentTerm() above should have returned true
						list.add(t);
						continue;
					}
				}
			return false;
		}
		if (list.isEmpty())
			return false;
		properties.put("content", Content.content_list);
		values.put("content", list);
		return true;
	}

	@SuppressWarnings("unused")
	private boolean processDisplay(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (d.size() != 1)
			return false;
		Term t = d.get(0);
		String prop = d.getProperty();
		if (genericTermIdent(Display.class, t, ALLOW_INH, prop, properties))
			return true;
		if (allowUnknownVendorExtensions)
			if (t instanceof TermIdent) {
				String ident = ((TermIdent)t).getValue();
				if (customIdentOrFuncName.matcher(ident).matches()) {
					for (BrailleCSSExtension x : extensions)
						if (ident.startsWith(x.getPrefix()))
							return false;
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
	private boolean processOrphans(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Orphans.class, Orphans.integer, true,
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
		if (d.size() == 1 && genericOneIdent(Size.class, d, properties))
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
			TermIdent textTransform = null;
			if (t instanceof TermIdent && ((TermIdent)t).getValue().startsWith("-")) {
				// if the counter name starts with an extension prefix, let the extension parse it
				for (BrailleCSSExtension x : extensions)
					if (((TermIdent)t).getValue().startsWith(x.getPrefix()))
						try {
							textTransform = x.parseTextTransform(t);
							break;
						} catch (IllegalArgumentException e) {
							return false;
						}
			} else 
				// other, give extensions the chance to normalize names
				for (BrailleCSSExtension x : extensions)
					try {
						textTransform = x.parseTextTransform(t);
						break;
					} catch (IllegalArgumentException e) {
						// continue
					}
			if (textTransform == null && t instanceof TermIdent)
				textTransform = (TermIdent)t;
			if (textTransform == null)
				return false;
			if (!"auto".equalsIgnoreCase(textTransform.getValue()))
				list.add(textTransform);
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
		if (allowUnknownVendorExtensions)
			if (d.size() == 1) {
				Term<?> term = d.get(0);
				if (term instanceof TermFunction) {
					String funcName = ((TermFunction)term).getFunctionName();
					if (customIdentOrFuncName.matcher(funcName).matches()) {
						for (BrailleCSSExtension x : extensions)
							if (funcName.startsWith(x.getPrefix()))
								return false;
						properties.put("volume-break-inside", VolumeBreakInside.custom);
						values.put("volume-break-inside", term);
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
	private boolean processWidows(Declaration d,
			Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		return genericOneIdentOrInteger(Widows.class, Widows.integer, true, d,
				properties, values);
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
				|| (genericTerm(TermIdent.class, d.get(0), d.getProperty(),
						identifierIdentification, sanify, properties, values)
					&& !((TermIdent)d.get(0)).getValue().startsWith("-"));
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
	
	///////////////////////////////////////////////////////////////
	// SupportedCSS
	///////////////////////////////////////////////////////////////

	@Override
	public boolean isSupportedMedia(String media) {
		return css.isSupportedMedia(media);
	}
	@Override
	public final boolean isSupportedCSSProperty(String property) {
		return css.isSupportedCSSProperty(property);
	}
	@Override
	public final CSSProperty getDefaultProperty(String property) {
		return css.getDefaultProperty(property);
	}
	@Override
	public final Term<?> getDefaultValue(String property) {
		return css.getDefaultValue(property);
	}
	@Override
	public final int getTotalProperties() {
		return css.getTotalProperties();
	}
	@Override
	public final Set<String> getDefinedPropertyNames() {
		return css.getDefinedPropertyNames();
	}
	@Override
	public String getRandomPropertyName() {
		return css.getRandomPropertyName();
	}
	@Override
	public int getOrdinal(String propertyName) {
		return css.getOrdinal(propertyName);
	}
	@Override
	public String getPropertyName(int o) {
		return css.getPropertyName(o);
	}
}

class SupportedCSSImpl implements SupportedCSS {

	private static Logger log = LoggerFactory.getLogger(SupportedBrailleCSS.class);

	private static final TermFactory tf = CSSFactory.getTermFactory();

	private static final CSSProperty DEFAULT_UA_TEXT_ALIGN = TextAlign.LEFT;
	private static final Term<?> DEFAULT_UA_TEXT_IDENT = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_MARGIN = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_PADDING = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_BORDER_WIDTH = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_ORPHANS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_WIDOWS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_LINE_HEIGHT = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_LETTER_SPACING = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_WORD_SPACING = tf.createInteger(1);

	private final int TOTAL_SUPPORTED_DECLARATIONS;

	private Set<String> supportedCSSproperties;
	private Map<String, CSSProperty> defaultCSSproperties;
	private Map<String, Term<?>> defaultCSSvalues;
	private Map<String, Integer> ordinals;
	private Map<Integer, String> ordinalsRev;

	SupportedCSSImpl(boolean allowComponentProperties, boolean allowShorthandProperties, Collection<BrailleCSSExtension> extensions) {
		this.TOTAL_SUPPORTED_DECLARATIONS = 70 + 2 * extensions.stream().mapToInt(BrailleCSSExtension::getTotalProperties).sum();
		this.setSupportedCSS(allowComponentProperties, allowShorthandProperties, extensions);
		this.setOridinals();
	}

	@Override
	public boolean isSupportedMedia(String media) {
		if (media == null)
			return false;
		return media.toLowerCase().equals("embossed");
	}

	@Override
	public final boolean isSupportedCSSProperty(String property) {
		if (supportedCSSproperties.contains(property))
			return true;
		else {
			if (defaultCSSproperties.containsKey(property))
				log.info("Shorthand or component property not supported: {}");
			return false; }
	}

	@Override
	public final CSSProperty getDefaultProperty(String property) {
		CSSProperty value = defaultCSSproperties.get(property);
		log.debug("Asked for property {}'s default value: {}", property, value);
		return value;
	}

	@Override
	public final Term<?> getDefaultValue(String property) {
		return defaultCSSvalues.get(property);
	}

	@Override
	public final int getTotalProperties() {
		return defaultCSSproperties.size();
	}

	@Override
	public final Set<String> getDefinedPropertyNames() {
		return defaultCSSproperties.keySet();
	}

	@Override
	public String getRandomPropertyName() {
		final Random generator = new Random();
		int o = generator.nextInt(getTotalProperties());
		return getPropertyName(o);
	}

	@Override
	public int getOrdinal(String propertyName) {
		Integer i = ordinals.get(propertyName);
		return (i == null) ? -1 : i.intValue();
	}

	@Override
	public String getPropertyName(int o) {
		return ordinalsRev.get(o);
	}

	private void setProperty(String name, CSSProperty defaultProperty) {
		setProperty(name, true, defaultProperty, null);
	}

	private void setProperty(String name, boolean allow, CSSProperty defaultProperty) {
		setProperty(name, allow, defaultProperty, null);
	}

	private void setProperty(String name, CSSProperty defaultProperty, Term<?> defaultValue) {
		setProperty(name, true, defaultProperty, defaultValue);
	}

	private void setProperty(String name, boolean allow, CSSProperty defaultProperty, Term<?> defaultValue) {
		if (allow)
			supportedCSSproperties.add(name);
		if (defaultProperty != null)
			defaultCSSproperties.put(name, defaultProperty);
		if (defaultValue != null)
			defaultCSSvalues.put(name, defaultValue);
	}

	private void setSupportedCSS(boolean allowComponentProperties, boolean allowShorthandProperties, Collection<BrailleCSSExtension> extensions) {

		supportedCSSproperties = new HashSet<String>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSproperties = new HashMap<String, CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSvalues = new HashMap<String, Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);;

		// text spacing
		setProperty("text-align", DEFAULT_UA_TEXT_ALIGN);
		setProperty("text-indent", TextIndent.integer, DEFAULT_UA_TEXT_IDENT);
		setProperty("line-height", LineHeight.number, DEFAULT_UA_LINE_HEIGHT);

		// layout box
		setProperty("left", AbsoluteMargin.AUTO);
		setProperty("right", AbsoluteMargin.AUTO);

		setProperty("margin-top", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin-right", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin-bottom", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin-left", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin", allowShorthandProperties, Margin.component_values);

		setProperty("padding-top", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding-right", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding-bottom", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding-left", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding", allowShorthandProperties, Padding.component_values);

		setProperty("border-top-pattern", allowComponentProperties, BorderPattern.NONE);
		setProperty("border-right-pattern", allowComponentProperties, BorderPattern.NONE);
		setProperty("border-bottom-pattern", allowComponentProperties, BorderPattern.NONE);
		setProperty("border-left-pattern", allowComponentProperties, BorderPattern.NONE);

		setProperty("border-top-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-right-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-bottom-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-left-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-style", allowShorthandProperties, BorderStyle.component_values);

		setProperty("border-top-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-right-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-bottom-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-left-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-width", allowShorthandProperties, BorderWidth.component_values);

		setProperty("border-top-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-right-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-bottom-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-left-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-align", allowShorthandProperties, BorderAlign.component_values);

		setProperty("border-top", allowShorthandProperties, Border.component_values);
		setProperty("border-right", allowShorthandProperties, Border.component_values);
		setProperty("border-bottom", allowShorthandProperties, Border.component_values);
		setProperty("border-left", allowShorthandProperties, Border.component_values);
		setProperty("border", allowShorthandProperties, Border.component_values);

		// positioning
		setProperty("display", Display.INLINE);

		// elements
		setProperty("list-style-type", ListStyleType.NONE);
		setProperty("list-style", allowShorthandProperties, ListStyleType.NONE);

		// @page rule
		setProperty("size", Size.AUTO);

		// paged
		setProperty("page", Page.AUTO);
		setProperty("page-break-before", PageBreak.AUTO);
		setProperty("page-break-after", PageBreak.AUTO);
		setProperty("page-break-inside", PageBreakInside.AUTO);
		setProperty("orphans", Orphans.integer, DEFAULT_UA_ORPHANS);
		setProperty("widows", Widows.integer, DEFAULT_UA_WIDOWS);

		// @footnotes rule
		setProperty("max-height", MaxHeight.NONE);

		// @volume rule
		setProperty("min-length", MinLength.AUTO);
		setProperty("max-length", MaxLength.AUTO);

		// volume breaking
		setProperty("volume-break-before", VolumeBreak.AUTO);
		setProperty("volume-break-after", VolumeBreak.AUTO);
		setProperty("volume-break-inside", VolumeBreakInside.AUTO);

		// tables
		setProperty("render-table-by", RenderTableBy.AUTO);
		setProperty("table-header-policy", TableHeaderPolicy.ONCE);

		// misc
		setProperty("counter-reset", CounterReset.NONE);
		setProperty("counter-set", CounterSet.NONE);
		setProperty("counter-increment", CounterIncrement.NONE);
		setProperty("string-set", StringSet.NONE);
		setProperty("content", Content.NONE);
		setProperty("text-transform", TextTransform.AUTO);
		setProperty("braille-charset", BrailleCharset.UNICODE);
		setProperty("white-space", WhiteSpace.NORMAL);
		setProperty("hyphenate-character", HyphenateCharacter.AUTO);
		setProperty("hyphens", Hyphens.MANUAL);
		setProperty("letter-spacing", LetterSpacing.length, DEFAULT_UA_LETTER_SPACING);
		setProperty("word-spacing", WordSpacing.length, DEFAULT_UA_WORD_SPACING);
		setProperty("flow", Flow.NORMAL);

		// vendor extensions
		for (BrailleCSSExtension x : extensions)
			for (String p : x.getDefinedPropertyNames())
				// p includes prefix
				setProperty(p, x.getDefaultProperty(p), x.getDefaultValue(p));
	}

	private void setOridinals() {
		Map<String, Integer> ords = new HashMap<String, Integer>(getTotalProperties(), 1.0f);
		Map<Integer, String> ordsRev = new HashMap<Integer, String>(getTotalProperties(), 1.0f);
		int i = 0;
		for (String key : defaultCSSproperties.keySet()) {
			ords.put(key, i);
			ordsRev.put(i, key);
			i++;
		}
		this.ordinals = ords;
		this.ordinalsRev = ordsRev;
	}
}
