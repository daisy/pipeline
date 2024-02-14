package org.daisy.pipeline.braille.dotify.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.BrailleCSSProperty.AbsoluteMargin;
import org.daisy.braille.css.BrailleCSSProperty.BorderPattern;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.Flow;
import org.daisy.braille.css.BrailleCSSProperty.TextIndent;
import org.daisy.braille.css.TermDotPattern;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.KeepWithSheets;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.Marker;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.ScenarioCost;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.TocRange;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.VerticalAlign;
import org.daisy.pipeline.braille.dotify.impl.OBFLProperty.VolumeBreakInside;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bert
 */
@Component(
	name = "org.daisy.pipeline.braille.dotify.impl.OBFLExtension",
	service = {
		BrailleCSSExtension.class
	}
)
public class OBFLExtension extends BrailleCSSExtension {

	private final String prefix;

	public OBFLExtension() {
		this("-obfl-");
	}

	/**
	 * @param prefix If specified, properties are only recognized if they begin with this
	 *               string. Must start and end with a '-'.
	 */
	public OBFLExtension(String prefix) {
		// SupportedOBFLProperties is defined at the bottom of this file
		// it is a separate class because we need an argument to pass to the constructor of BrailleCSSExtension
		super(new SupportedOBFLProperties());
		if (prefix == null)
			throw new IllegalArgumentException();
		this.prefix = prefix;
		this.methods = findParsingMethods();
	}

	///////////////////////////////////////////////////////////////
	// BrailleCSSExtension
	///////////////////////////////////////////////////////////////

	@Override
	public String getPrefix() {
		return prefix;
	}

	private final static Set<String> customContentFuncNames = new HashSet<String>(Arrays.asList("-obfl-evaluate",
	                                                                                            "-obfl-marker-indicator",
	                                                                                            "-obfl-collection"));

	@Override
	public boolean parseContentTerm(Term<?> term, TermList list) {
		if (term instanceof TermFunction) {
			String funcName = ((TermFunction)term).getFunctionName();
			if (customContentFuncNames.contains(funcName)) {
				list.add(term);
				return true;
			}
		}
		return false;
	}

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	private Map<String,Method> findParsingMethods() {
		Map<String,Method> map = new HashMap<String,Method>(css.getTotalProperties(), 1.0f);
		for (String property : css.getDefinedPropertyNames()) {
			try {
				Method m = OBFLExtension.class.getDeclaredMethod(
					camelCase("process-" + property),
					Declaration.class, Map.class, Map.class);
				map.put(prefix + property, m);
			} catch (Exception e) {
				log.debug("Unable to find method for property {}.", property); // should not happen
			}
		}
		// add parsing methods for standard properties from SupportedBrailleCSS
		for (String property : new String[]{"display",
		                                    "volume-break-inside"}) {
			try {
				Method m = OBFLExtension.class.getDeclaredMethod(
					camelCase("process-" + property),
					Declaration.class, Map.class, Map.class);
				map.put(property, m);
			} catch (Exception e) {
				log.debug("Unable to find method for property {}.", property); // should not happen
			}
		}
		log.debug("Totally found {} parsing methods", map.size());
		return map;
	}

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		String property = d.getProperty().toLowerCase();
		try {
			Method m = methods.get(property);
			if (m != null)
				try {
					return (Boolean)m.invoke(this, d, properties, values);
				} catch (IllegalAccessException e) {
				} catch (IllegalArgumentException e) {
				} catch (InvocationTargetException e) {
				}
		} catch (Exception e) {
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processDisplay(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (d.size() != 1)
			return false;
		Term t = d.get(0);
		if (t instanceof TermIdent &&
		    ("-obfl-toc".equals(((TermIdent)t).getValue()) ||
		     "-obfl-list-of-references".equals(((TermIdent)t).getValue()))) {
			String prop = d.getProperty();
			properties.put(prop, Display.custom);
			values.put(prop, t);
			return true;
		}
		return false;
	}

	private boolean processFallbackCollection(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrIdentifier(Flow.class, Flow.identifier, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processFallbackFlow(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return processFallbackCollection(d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processKeepWithNextSheets(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(KeepWithSheets.class, KeepWithSheets.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processKeepWithPreviousSheets(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(KeepWithSheets.class, KeepWithSheets.integer, true, d, properties, values);
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
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processRightTextIndent(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(TextIndent.class, TextIndent.integer, false, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processScenarioCost(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (d.size() != 1)
			return false;
		if (genericOneIdentOrInteger(ScenarioCost.class, ScenarioCost.integer, false, d, properties, values))
			return true;
		Term term = d.get(0);
		if (term instanceof TermFunction) {
			TermFunction fun = (TermFunction)term;
			if ("-obfl-evaluate".equals(fun.getFunctionName())
			    && fun.size() == 1
			    && fun.get(0) instanceof TermString) {
				String prop = d.getProperty();
				properties.put(prop, ScenarioCost.evaluate);
				values.put(prop, fun);
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processTableColSpacing(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processTableRowSpacing(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processTocRange(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdent(TocRange.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processUnderline(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrDotPattern(
			BorderPattern.class, BorderPattern.dot_pattern,d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processUseWhenCollectionNotEmpty(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return processFallbackCollection(d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processVerticalAlign(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdent(VerticalAlign.class, d, properties);
	}

	@SuppressWarnings("unused")
	private boolean processVerticalPosition(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(AbsoluteMargin.class, AbsoluteMargin.integer, true, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processVolumeBreakInside(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (d.size() == 1) {
			Term<?> term = d.get(0);
			if (term instanceof TermFunction) {
				TermFunction fun = (TermFunction)term;
				if ("-obfl-keep".equals(fun.getFunctionName())
					&& fun.size() == 1
					&& fun.get(0) instanceof TermInteger) {
					String prop = d.getProperty();
					properties.put(prop, VolumeBreakInside.keep);
					values.put(prop, fun);
					return true;
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

class SupportedOBFLProperties implements SupportedCSS {

	private static Logger log = LoggerFactory.getLogger(SupportedOBFLProperties.class);

	private static final int TOTAL_SUPPORTED_DECLARATIONS = 15;

	private static final TermFactory tf = CSSFactory.getTermFactory();

	private static final Term<?> DEFAULT_UA_TEXT_IDENT = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_KEEP_WITH_PREVIOUS_SHEETS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_KEEP_WITH_NEXT_SHEETS = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_PREFERRED_EMPTY_SPACE = tf.createInteger(2);

	private Set<String> supportedCSSproperties;
	private Map<String,CSSProperty> defaultCSSproperties;
	private Map<String,Term<?>> defaultCSSvalues;
	private Map<String,Integer> ordinals;
	private Map<Integer,String> ordinalsRev;

	SupportedOBFLProperties() {
		this.setSupportedCSS();
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

	private void setProperty(String name, CSSProperty defaultProperty, Term<?> defaultValue) {
		supportedCSSproperties.add(name);
		if (defaultProperty != null)
			defaultCSSproperties.put(name, defaultProperty);
		if (defaultValue != null)
			defaultCSSvalues.put(name, defaultValue);
	}

	private void setSupportedCSS() {
		supportedCSSproperties = new HashSet<String>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSproperties = new HashMap<String,CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSvalues = new HashMap<String,Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);;

		setProperty("right-text-indent", TextIndent.integer, DEFAULT_UA_TEXT_IDENT);
		setProperty("underline", BorderPattern.NONE, null);
		setProperty("fallback-collection", Flow.NORMAL, null);
		setProperty("fallback-flow", Flow.NORMAL, null);
		setProperty("keep-with-next-sheets", KeepWithSheets.integer, DEFAULT_UA_KEEP_WITH_NEXT_SHEETS);
		setProperty("keep-with-previous-sheets", KeepWithSheets.integer, DEFAULT_UA_KEEP_WITH_PREVIOUS_SHEETS);
		setProperty("marker", Marker.NONE, null);
		setProperty("preferred-empty-space", AbsoluteMargin.integer, DEFAULT_UA_PREFERRED_EMPTY_SPACE);
		setProperty("scenario-cost", ScenarioCost.NONE, null);
		setProperty("table-col-spacing", AbsoluteMargin.AUTO, null);
		setProperty("table-row-spacing", AbsoluteMargin.AUTO, null);
		setProperty("toc-range", TocRange.DOCUMENT, null);
		setProperty("use-when-collection-not-empty", Flow.NORMAL, null);
		setProperty("vertical-align", VerticalAlign.AFTER, null);
		setProperty("vertical-position", AbsoluteMargin.AUTO, null);
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
