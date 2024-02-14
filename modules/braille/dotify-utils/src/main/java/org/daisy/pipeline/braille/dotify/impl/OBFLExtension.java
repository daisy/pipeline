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

	private final static String prefix = "-obfl-";

	public OBFLExtension() {
		// SupportedOBFLProperties is defined at the bottom of this file
		// it is a separate class because we need an argument to pass to the constructor of BrailleCSSExtension
		super(new SupportedOBFLProperties(prefix));
		this.methods = findParsingMethods();
	}

	///////////////////////////////////////////////////////////////
	// BrailleCSSExtension
	///////////////////////////////////////////////////////////////

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public boolean parseContentTerm(Term<?> term, TermList list) {
		if (term instanceof TermFunction) {
			TermFunction f = (TermFunction)term;
			String funcName = f.getFunctionName();
			boolean normalize = false;
			if (!funcName.startsWith(prefix)) {
				funcName = prefix + funcName;
				normalize = true;
			}
			if ((prefix + "evaluate").equals(funcName)) {
				if (f.size() != 1)
					return false;
				if (!(f.get(0) instanceof TermString))
					return false;
			} else if ((prefix + "marker-indicator").equals(funcName)) {
				if (f.size() != 2)
					return false;
				if (!(f.get(0) instanceof TermIdent))
					return false;
				if (!(f.get(1) instanceof TermString))
					return false;
			} else if ((prefix + "collection").equals(funcName)) {
				if (f.size() < 1 || f.size() > 2)
					return false;
				if (!(f.get(0) instanceof TermIdent))
					return false;
				if (f.size() == 2 && !(f.get(1) instanceof TermIdent))
					return false;
			} else
				return false;
			if (normalize)
				f = f.setFunctionName(funcName);
			list.add(term);
			return true;
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
					camelCase("process-" + (property.startsWith(prefix) ? property.substring(prefix.length()) : property)),
					Declaration.class, Map.class, Map.class);
				map.put(property, m);
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
		// note that SupportedBrailleCSS already normalizes property names, so no need to do it here
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
		} catch (Exception e) {
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean processDisplay(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
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
					properties.put(d.getProperty(), org.daisy.pipeline.braille.dotify.impl.OBFLProperty.VolumeBreakInside.keep);
					if (normalize)
						f = f.setFunctionName(funcName);
					values.put(d.getProperty(), f);
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

	SupportedOBFLProperties(String prefix) {
		this.setSupportedCSS(prefix);
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

	private void setSupportedCSS(String prefix) {
		supportedCSSproperties = new HashSet<String>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSproperties = new HashMap<String,CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSvalues = new HashMap<String,Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);;
		if (prefix == null) prefix = "";

		setProperty(prefix + "right-text-indent", TextIndent.integer, DEFAULT_UA_TEXT_IDENT);
		setProperty(prefix + "underline", BorderPattern.NONE, null);
		setProperty(prefix + "fallback-collection", Flow.NORMAL, null);
		setProperty(prefix + "fallback-flow", Flow.NORMAL, null);
		setProperty(prefix + "keep-with-next-sheets", KeepWithSheets.integer, DEFAULT_UA_KEEP_WITH_NEXT_SHEETS);
		setProperty(prefix + "keep-with-previous-sheets", KeepWithSheets.integer, DEFAULT_UA_KEEP_WITH_PREVIOUS_SHEETS);
		setProperty(prefix + "marker", Marker.NONE, null);
		setProperty(prefix + "preferred-empty-space", AbsoluteMargin.integer, DEFAULT_UA_PREFERRED_EMPTY_SPACE);
		setProperty(prefix + "scenario-cost", ScenarioCost.NONE, null);
		setProperty(prefix + "table-col-spacing", AbsoluteMargin.AUTO, null);
		setProperty(prefix + "table-row-spacing", AbsoluteMargin.AUTO, null);
		setProperty(prefix + "toc-range", TocRange.DOCUMENT, null);
		setProperty(prefix + "use-when-collection-not-empty", Flow.NORMAL, null);
		setProperty(prefix + "vertical-align", VerticalAlign.AFTER, null);
		setProperty(prefix + "vertical-position", AbsoluteMargin.AUTO, null);
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
