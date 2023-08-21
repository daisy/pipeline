package org.daisy.pipeline.braille.dotify.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.PageBreak;
import cz.vutbr.web.css.CSSProperty.PageBreakInside;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSProperty;
import org.daisy.braille.css.BrailleCSSProperty.AbsoluteMargin;
import org.daisy.braille.css.BrailleCSSProperty.BorderPattern;
import org.daisy.braille.css.BrailleCSSProperty.BrailleCharset;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.Flow;
import org.daisy.braille.css.BrailleCSSProperty.HyphenateCharacter;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.LetterSpacing;
import org.daisy.braille.css.BrailleCSSProperty.Margin;
import org.daisy.braille.css.BrailleCSSProperty.Orphans;
import org.daisy.braille.css.BrailleCSSProperty.Padding;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreak;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreakInside;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.BrailleCSSProperty.Widows;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;
import org.daisy.braille.css.PropertyValue;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.braille.css.TermDotPattern;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.KeepWithSheets;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.LineHeight;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.Marker;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.ScenarioCost;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.TextAlign;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.TextIndent;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.TocRange;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.VerticalAlign;
import org.daisy.pipeline.braille.dotify.saxon.impl.ObflStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bert
 */
// this class extends SupportedBrailleCSS so that it can be used in OBFLStyleTransformer
public class SupportedOBFLProperties extends SupportedBrailleCSS {

	private final boolean forExtension;
	private final boolean forTransformer;
	private final String prefix;

	/**
	 * @param forExtension For use in {@code OBFLExtension}. Includes the non-standard (-obfl- prefixed)
	 *                     CSS properties.
	 * @param forTransformer For use in {@code ObflStyle}. Includes all properties supported by OBFL.
	 *                       Some properties have different defaults or other allowed values than in
	 *                       standard CSS.
	 */
	public SupportedOBFLProperties(boolean forExtension, boolean forTransformer, String prefix) {
		// SupportedCSSImpl is defined at the bottom of this file
		// it is a separate class because we need an argument to pass to the constructor of DeclarationTransformer
		super(new SupportedCSSImpl(forExtension, forTransformer, prefix));
		if (forExtension == forTransformer)
			throw new IllegalArgumentException();
		this.forExtension = forExtension;
		this.forTransformer = forTransformer;
		this.prefix = prefix != null ? prefix : "";
		this.methods = findParsingMethods();
	}

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	private Map<String,Method> findParsingMethods() {
		Map<String,Method> map = new HashMap<String,Method>(css.getTotalProperties(), 1.0f);
		for (String property : css.getDefinedPropertyNames()) {
			try {
				String methodName = camelCase(
					"process-" + ((prefix.length() > 0 && property.startsWith(prefix))
					                  ? property.substring(prefix.length())
					                  : property));
				Method m = SupportedOBFLProperties.class.getDeclaredMethod(
					methodName,
					Declaration.class, Map.class, Map.class);
				map.put(property, m);
			} catch (Exception e) { // assuming this is a NoSuchMethodException
				if (forTransformer)
					// we're calling from OBFLStyleTransformer, so in the absence of a parsing method
					// we can pass the (already parsed) declarations as-is
					;
				else
					log.debug("Unable to find method for property {}.", property); // should not happen
			}
		}
		if (forExtension) {
			// add parsing methods for standard properties from SupportedBrailleCSS
			for (String property : new String[]{"display",
			                                    "volume-break-inside"}) {
				try {
					Method m = SupportedOBFLProperties.class.getDeclaredMethod(
						camelCase("process-" + property),
						Declaration.class, Map.class, Map.class);
					map.put(property, m);
				} catch (Exception e) {
					log.debug("Unable to find method for property {}.", property); // should not happen
				}
			}
		}
		log.debug("Totally found {} parsing methods", map.size());
		return map;
	}

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		String property = d.getProperty().toLowerCase();
		if (!(css.isSupportedCSSProperty(property) || "display".equals(property) || "volume-break-inside".equals(property))) {
			if (!property.startsWith(prefix) && css.isSupportedCSSProperty(prefix + property))
				property = prefix + property;
			else
				return false;
		}
		try {
			Method m = methods.get(property);
			if (m != null)
				try {
					return (Boolean)m.invoke(this, d, properties, values);
				} catch (IllegalAccessException e) {
				} catch (IllegalArgumentException e) {
				} catch (InvocationTargetException e) {
				}
			else
				// if there is no parsing method for a property, but the property is supported,
				// pass the (already parsed) declaration as-is
				if (forTransformer && css.isSupportedCSSProperty(property))
					// we're calling from OBFLStyleTransformer, so declaration is a PropertyValue
					return copyPropertyValue(d, properties, values);
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * @param d must be a {@link PropertyValue}
	 */
	private boolean copyPropertyValue(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (!(d instanceof PropertyValue))
			throw new RuntimeException("coding error");
		properties.put(d.getProperty(), ((PropertyValue)d).getCSSProperty());
		Term<?> value = ((PropertyValue)d).getValue();
		if (value != null)
			values.put(d.getProperty(), value);
		return true;
	}

	@SuppressWarnings("unused")
	private boolean processDisplay(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		if (d.size() != 1)
			return false;
		Term t = d.get(0);
		if (t instanceof TermIdent) {
			String display = ((TermIdent)t).getValue();
			boolean normalize = false;
			if (!display.startsWith(prefix)) {
				display = prefix + display;
				normalize = true;
			}
			if ((prefix + "toc").equals(display) ||
			    (prefix + "list-of-references").equals(display)) {
				String prop = d.getProperty();
				properties.put(prop, Display.custom);
				if (normalize)
					t = ((TermIdent)t).setValue(display);
				values.put(prop, t);
				return true;
			}
		}
		return false;
	}

	private boolean processFallbackCollection(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrIdentifier(Flow.class, Flow.identifier, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processFallbackFlow(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		log.warn("Correct spelling of '" + prefix + "fallback-flow' is '" + prefix + "fallback-collection'");
		if (processFallbackCollection(d, properties, values)) {
			// normalize
			if (properties.containsKey(prefix + "fallback-flow"))
				properties.put(prefix + "fallback-collection", properties.remove(prefix + "fallback-flow"));
			if (values.containsKey(prefix + "fallback-flow"))
				values.put(prefix + "fallback-collection", values.remove(prefix + "fallback-flow"));
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processKeepWithNextSheets(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return processKeepWithSheets(d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processKeepWithPreviousSheets(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return processKeepWithSheets(d, properties, values);
	}

	private boolean processKeepWithSheets(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		if (d.size() != 1)
			return false;
		Term<?> t = d.get(0);
		String prop = d.getProperty();
		if (genericTermIdent(KeepWithSheets.class, t, false, prop, properties))
			return true;
		if (!(t instanceof TermInteger))
			return false;
		int i = ((TermInteger)t).getIntValue();
		if (i >= 0 && i <= 9) {
			properties.put(prop, KeepWithSheets.integer);
			values.put(prop, t);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean processLineHeight(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer) {
			if (!(d instanceof PropertyValue))
				throw new RuntimeException("coding error");
			String p = d.getProperty();
			Term<?> v = ((PropertyValue)d).getValue();
			switch ((BrailleCSSProperty.LineHeight)((PropertyValue)d).getCSSProperty()) {
			case number:
				properties.put(p, LineHeight.number);
				values.put(p, v);
				break;
			case percentage:
				properties.put(p, LineHeight.percentage);
				values.put(p, v);
				return true;
			case INHERIT:
				properties.put(p, LineHeight.INHERIT);
				return true;
			case INITIAL:
				properties.put(p, LineHeight.INITIAL);
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processMarker(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		TermList list = tf.createList();
		for (Term<?> t : d.asList()) {
			if (t instanceof TermIdent)
				list.add(t);
			else
				return false;
		}
		if (list.isEmpty())
			return false;
		properties.put(d.getProperty(), Marker.list_values);
		values.put(d.getProperty(), list);
		return true;
	}

	@SuppressWarnings("unused")
	private boolean processPreferredEmptySpace(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processRightTextIndent(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdentOrInteger(TextIndent.class, TextIndent.integer, false, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processScenarioCost(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		if (d.size() != 1)
			return false;
		if (genericOneIdentOrInteger(ScenarioCost.class, ScenarioCost.integer, false, d, properties, values))
			return true;
		Term term = d.get(0);
		if (term instanceof TermFunction) {
			TermFunction f = (TermFunction)term;
			String funcName = f.getFunctionName();
			boolean normalize = false;
			if (!funcName.startsWith(prefix)) {
				funcName = prefix + funcName;
				normalize = true;
			}
			if ((prefix + "evaluate").equals(funcName)
			    && f.size() == 1
			    && f.get(0) instanceof TermString) {
				String prop = d.getProperty();
				properties.put(prop, ScenarioCost.evaluate);
				if (normalize)
					f = f.setFunctionName(funcName);
				values.put(prop, f);
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processTableColSpacing(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processTableRowSpacing(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processTextAlign(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer) {
			if (!(d instanceof PropertyValue))
				throw new RuntimeException("coding error");
			String p = d.getProperty();
			switch ((BrailleCSSProperty.TextAlign)((PropertyValue)d).getCSSProperty()) {
			case INHERIT:
				properties.put(p, TextAlign.INHERIT);
				return true;
			case INITIAL:
				properties.put(p, TextAlign.INITIAL);
				return true;
			case LEFT:
				properties.put(p, TextAlign.LEFT);
				return true;
			case CENTER:
				properties.put(p, TextAlign.CENTER);
				return true;
			case RIGHT:
				properties.put(p, TextAlign.RIGHT);
				return true;
			case BY_DIRECTION:
			case JUSTIFY:
			default:
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processTextIndent(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer) {
			if (!(d instanceof PropertyValue))
				throw new RuntimeException("coding error");
			String p = d.getProperty();
			switch ((BrailleCSSProperty.TextIndent)((PropertyValue)d).getCSSProperty()) {
			case INHERIT:
				properties.put(p, TextIndent.INHERIT);
				return true;
			case INITIAL:
				properties.put(p, TextIndent.INITIAL);
				return true;
			case integer:
				properties.put(p, TextIndent.integer);
				values.put(p, ((PropertyValue)d).getValue());
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processTocRange(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdent(TocRange.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processUnderline(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdentOrDotPattern(
			BorderPattern.class, BorderPattern.dot_pattern,d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processUseWhenCollectionNotEmpty(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return processFallbackCollection(d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processVerticalAlign(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdent(VerticalAlign.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processVerticalPosition(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer)
			return copyPropertyValue(d, properties, values);
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processVolumeBreakAfter(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return processVolumeBreak(d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processVolumeBreakBefore(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return processVolumeBreak(d, properties, values);
	}

	private boolean processVolumeBreak(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer) {
			if (!(d instanceof PropertyValue))
				throw new RuntimeException("coding error");
			VolumeBreak property = (VolumeBreak)((PropertyValue)d).getCSSProperty();
			if (property == VolumeBreak.AVOID || property == VolumeBreak.PREFER)
				return false;
			properties.put(d.getProperty(), property);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processVolumeBreakInside(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (forTransformer) {
			if (!(d instanceof PropertyValue))
				throw new RuntimeException("coding error");
			CSSProperty property = ((PropertyValue)d).getCSSProperty();
			if (property == VolumeBreakInside.AVOID)
				return false;
			return copyPropertyValue(d, properties, values);
		}
		if (d.size() == 1) {
			Term<?> term = d.get(0);
			if (term instanceof TermFunction) {
				TermFunction f = (TermFunction)term;
				String funcName = f.getFunctionName();
				boolean normalize = false;
				if (!funcName.startsWith(prefix)) {
					funcName = prefix + funcName;
					normalize = true;
				}
				if ((prefix + "keep").equals(funcName)
					&& f.size() == 1
					&& f.get(0) instanceof TermInteger) {
					int priority = ((TermInteger)f.get(0)).getIntValue();
					if (priority >= 1 && priority <= 9) {
						properties.put(d.getProperty(), org.daisy.pipeline.braille.dotify.impl.OBFLProperty.VolumeBreakInside.keep);
						if (normalize)
							f = f.setFunctionName(funcName);
						values.put(d.getProperty(), f);
						return true;
					}
				}
			}
		}
		return false;
	}

	// not reusing SupportedBrailleCSS#genericOneIdentOrDotPattern because method is not static
	private <T extends CSSProperty> boolean genericOneIdentOrDotPattern(
		Class<T> type, T dotPatternIdentification, Declaration d,
		Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (d.size() != 1)
			return false;
		Term<?> term = d.get(0);
		if (genericTermIdent(type, term, ALLOW_INH, d.getProperty(), properties))
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

	// not reusing SupportedBrailleCSS#genericOneIdentOrIdentifier because method is not static
	private <T extends CSSProperty> boolean genericOneIdentOrIdentifier(
		Class<T> type, T identifierIdentification, boolean sanify,
		Declaration d, Map<String,CSSProperty> properties,
		Map<String,Term<?>> values) {
		if (d.size() != 1)
			return false;
		return genericTermIdent(type, d.get(0), ALLOW_INH, d.getProperty(),
				properties)
				|| (genericTerm(TermIdent.class, d.get(0), d.getProperty(),
						identifierIdentification, sanify, properties, values)
					&& !((TermIdent)d.get(0)).getValue().startsWith("-"));
	}

}

class SupportedCSSImpl implements SupportedCSS {

	private static Logger log = LoggerFactory.getLogger(SupportedOBFLProperties.class);

	private static final int TOTAL_SUPPORTED_DECLARATIONS = 15;

	private static final TermFactory tf = CSSFactory.getTermFactory();

	private static final Term<?> DEFAULT_UA_MARGIN = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_PADDING = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_LINE_HEIGHT = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_LETTER_SPACING = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_WORD_SPACING = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_ORPHANS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_WIDOWS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_TEXT_IDENT = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_KEEP_WITH_PREVIOUS_SHEETS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_KEEP_WITH_NEXT_SHEETS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_PREFERRED_EMPTY_SPACE = tf.createInteger(2);

	private Set<String> supportedCSSproperties;
	private Map<String,CSSProperty> defaultCSSproperties;
	private Map<String,Term<?>> defaultCSSvalues;
	private Map<String,Integer> ordinals;
	private Map<Integer,String> ordinalsRev;

	SupportedCSSImpl(boolean forExtension, boolean forTransformer, String prefix) {
		this.setSupportedCSS(forExtension, forTransformer, prefix);
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
		return supportedCSSproperties.contains(property);
	}

	@Override
	public final CSSProperty getDefaultProperty(String property) {
		CSSProperty value = defaultCSSproperties.get(property);
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
		setProperty(name, defaultProperty, null);
	}
	
	private void setProperty(String name, CSSProperty defaultProperty, Term<?> defaultValue) {
		supportedCSSproperties.add(name);
		if (defaultProperty != null)
			defaultCSSproperties.put(name, defaultProperty);
		if (defaultValue != null)
			defaultCSSvalues.put(name, defaultValue);
	}

	private void setSupportedCSS(boolean forExtension, boolean forTransformer, String prefix) {
		supportedCSSproperties = new HashSet<String>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSproperties = new HashMap<String,CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSvalues = new HashMap<String,Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		if (prefix == null) prefix = "";

		// standard properties supported in OBFL

		if (forTransformer) {
			setProperty("border-bottom-pattern", BorderPattern.NONE);
			setProperty("border-left-pattern",   BorderPattern.NONE);
			setProperty("border-right-pattern",  BorderPattern.NONE);
			setProperty("border-top-pattern",    BorderPattern.NONE);
			setProperty("braille-charset",       BrailleCharset.UNICODE);
			setProperty("hyphenate-character",   HyphenateCharacter.AUTO);
			setProperty("hyphens",               Hyphens.MANUAL);
			setProperty("letter-spacing",        LetterSpacing.length,    DEFAULT_UA_LETTER_SPACING);
			setProperty("line-height",           LineHeight.number,       DEFAULT_UA_LINE_HEIGHT);
			setProperty("margin-bottom",         Margin.integer,          DEFAULT_UA_MARGIN);
			setProperty("margin-left",           Margin.integer,          DEFAULT_UA_MARGIN);
			setProperty("margin-right",          Margin.integer,          DEFAULT_UA_MARGIN);
			setProperty("margin-top",            Margin.integer,          DEFAULT_UA_MARGIN);
			setProperty("orphans",               Orphans.integer,         DEFAULT_UA_ORPHANS);
			setProperty("padding-bottom",        Padding.integer,         DEFAULT_UA_PADDING);
			setProperty("padding-left",          Padding.integer,         DEFAULT_UA_PADDING);
			setProperty("padding-right",         Padding.integer,         DEFAULT_UA_PADDING);
			setProperty("padding-top",           Padding.integer,         DEFAULT_UA_PADDING);
			setProperty("page-break-after",      PageBreak.AUTO);
			setProperty("page-break-before",     PageBreak.AUTO);
			setProperty("page-break-inside",     PageBreakInside.AUTO);
			setProperty("text-align",            TextAlign.LEFT);
			setProperty("text-indent",           TextIndent.integer,      DEFAULT_UA_TEXT_IDENT);
			setProperty("text-transform",        TextTransform.AUTO);
			setProperty("volume-break-after",    VolumeBreak.AUTO);
			setProperty("volume-break-before",   VolumeBreak.AUTO);
			setProperty("volume-break-inside",   VolumeBreakInside.AUTO);
			setProperty("white-space",           WhiteSpace.NORMAL);
			setProperty("widows",                Widows.integer,          DEFAULT_UA_WIDOWS);
			setProperty("word-spacing",          WordSpacing.length,      DEFAULT_UA_WORD_SPACING);
		}

		// OBFL specific properties

		setProperty(prefix + "right-text-indent",             TextIndent.integer,     DEFAULT_UA_TEXT_IDENT);
		setProperty(prefix + "underline",                     BorderPattern.NONE);
		setProperty(prefix + "keep-with-next-sheets",         KeepWithSheets.integer, DEFAULT_UA_KEEP_WITH_NEXT_SHEETS);
		setProperty(prefix + "keep-with-previous-sheets",     KeepWithSheets.integer, DEFAULT_UA_KEEP_WITH_PREVIOUS_SHEETS);
		setProperty(prefix + "preferred-empty-space",         AbsoluteMargin.integer, DEFAULT_UA_PREFERRED_EMPTY_SPACE);
		setProperty(prefix + "scenario-cost",                 ScenarioCost.NONE);
		setProperty(prefix + "table-col-spacing",             AbsoluteMargin.AUTO);
		setProperty(prefix + "table-row-spacing",             AbsoluteMargin.AUTO);
		setProperty(prefix + "toc-range",                     TocRange.DOCUMENT);
		setProperty(prefix + "use-when-collection-not-empty", Flow.NORMAL);
		setProperty(prefix + "vertical-align",                VerticalAlign.AFTER);
		setProperty(prefix + "vertical-position",             AbsoluteMargin.AUTO);

		if (forExtension) {
			setProperty(prefix + "fallback-collection", Flow.NORMAL, null);
			setProperty(prefix + "fallback-flow", Flow.NORMAL, null);
			setProperty(prefix + "marker", Marker.NONE, null);
		}

		// the following properties are not supported in OBFL (generate a warning for
		// values other than the default):

		// - border-bottom-align
		// - border-bottom-style
		// - border-bottom-width
		// - border-left-align
		// - border-left-style
		// - border-left-width
		// - border-right-align
		// - border-right-style
		// - border-right-width
		// - border-top-align
		// - border-top-style
		// - border-top-width
		// - left
		// - right

		// - content (already handled in css:parse-content)
		// - counter-increment (already handled in css:eval-counter)
		// - counter-reset (already handled in css:eval-counter)
		// - counter-set (already handled in css:eval-counter)
		// - display (already handled in css:make-boxes)
		// - flow (already handled in css:flow-into and css:flow-from)
		// - list-style-type (already handled in css:make-boxes)
		// - page (already handled in px:css-cascade)
		// - render-table-by (already handled in css:render-table-by)
		// - string-set (already handled in css:eval-string-set and css:shift-string-set)
		// - table-header-policy (already handled in css:render-table-by)
	}

	private void setOridinals() {
		Map<String,Integer> ords = new HashMap<String,Integer>(getTotalProperties(), 1.0f);
		Map<Integer,String> ordsRev = new HashMap<Integer,String>(getTotalProperties(), 1.0f);
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
