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
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;
import cz.vutbr.web.css.TermIdent;

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.BrailleCSSProperty.BorderPattern;
import org.daisy.braille.css.BrailleCSSProperty.TextIndent;

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
		this.prefix = prefix;
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
			String funcName = ((TermFunction)t).getFunctionName();
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

	@Override
	protected Map<String,Method> parsingMethods() {
		Map<String,Method> map = new HashMap<String,Method>(css.getTotalProperties(), 1.0f);
		for (String property : css.getDefinedPropertyNames()) {
			try {
				Method m = OBFLExtension.class.getDeclaredMethod(
					camelCase("process-" + property),
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
		if (prev)
		log.debug("Totally found {} parsing methods", map.size());
		return map;
	}

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		String property = d.getProperty().toLowerCase();
		if (prefix != null && property.startsWith(prefix))
			property = property.substring(prefix.length());
		if (prefix == null && !css.isSupportedCSSProperty(property))
			return false;
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
	private boolean processRightTextIndent(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(TextIndent.class, TextIndent.integer, false, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processUnderline(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrDotPattern(
			BorderPattern.class, BorderPattern.dot_pattern,d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processDisplay(Declaration d, Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
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

	@SuppressWarnings("unused")
	private boolean processVolumeBreakInside(Declaration d, Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		if (d.size() == 1) {
			Term<?> term = d.get(0);
			if (term instanceof TermFunction) {
				TermFunction fun = (TermFunction)term;
				if ("-obfl-keep".equals(fun.getFunctionName())
					&& fun.size() == 1
					&& fun.get(0) instanceof TermInteger) {
					String prop = d.getProperty();
					properties.put(prop, VolumeBreakInside.obfl_keep);
					values.put(prop, fun);
					return true;
				}
			}
		}
		return false;
	}

	// not reusing BrailleCSSDeclarationTransformer#genericOneIdentOrDotPattern because method is not static
	private <T extends CSSProperty> boolean genericOneIdentOrDotPattern(
		Class<T> type, T dotPatternIdentification, Declaration d,
		Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
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

class SupportedOBFLProperties implements SupportedCSS {

	private static Logger log = LoggerFactory.getLogger(SupportedOBFLProperties.class);

	private static final int TOTAL_SUPPORTED_DECLARATIONS = 2;

	private static final TermFactory tf = CSSFactory.getTermFactory();

	private static final Term<?> DEFAULT_UA_TEXT_IDENT = tf.createInteger(0);

	private Set<String> supportedCSSproperties;
	private Map<String, CSSProperty> defaultCSSproperties;
	private Map<String, Term<?>> defaultCSSvalues;
	private Map<String, Integer> ordinals;
	private Map<Integer, String> ordinalsRev;

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
		defaultCSSproperties = new HashMap<String, CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSvalues = new HashMap<String, Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);;

		setProperty("right-text-indent", TextIndent.integer, DEFAULT_UA_TEXT_IDENT);
		setProperty("underline", BorderPattern.NONE, null);
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
