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
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermFactory;

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.BrailleCSSProperty.ListStyleType;
import org.daisy.pipeline.css.CounterStyle;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: ideally this extension should only be active within px:obfl-to-pef
@Component(
	name = "org.daisy.pipeline.braille.dotify.impl.DotifyCSSExtension",
	service = {
		BrailleCSSExtension.class
	}
)
public class DotifyCSSExtension extends BrailleCSSExtension {

	private final static String prefix = "-dotify-";

	public DotifyCSSExtension() {
		// SupportedDotifyProperties is defined at the bottom of this file
		// it is a separate class because we need an argument to pass to the constructor of BrailleCSSExtension
		super(new SupportedDotifyProperties(prefix));
		this.methods = findParsingMethods();
	}

	///////////////////////////////////////////////////////////////
	// BrailleCSSExtension
	///////////////////////////////////////////////////////////////

	@Override
	public String getPrefix() {
		return prefix;
	}

	public TermIdent parseTextTransform(Term<?> term) {
		if (term instanceof TermIdent) {
			TermIdent ident = (TermIdent)term;
			if ((prefix + "counter").equals(ident.getValue()))
				return ident;
		}
		throw new IllegalArgumentException("Unknown text-transform value " + term);
	}

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	private Map<String,Method> findParsingMethods() {
		Map<String,Method> map = new HashMap<String,Method>(css.getTotalProperties(), 1.0f);
		for (String property : css.getDefinedPropertyNames()) {
			try {
				Method m = DotifyCSSExtension.class.getDeclaredMethod(
					camelCase("process-" + property.substring(prefix.length())),
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
		if (!(css.isSupportedCSSProperty(property)))
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
	private boolean processCounterStyle(Declaration d, Map<String, CSSProperty> properties, Map<String, Term<?>> values) {
		String propertyName = d.getProperty();
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
				else if (TermFunction.class.isInstance(term)
				         && "symbols".equals(((TermFunction)term).getFunctionName().toLowerCase())) {
					properties.put(propertyName, ListStyleType.symbols_fn);
					values.put(propertyName, term);
					return true; }}
			catch (Exception e) {}
		return false;
	}
}

class SupportedDotifyProperties implements SupportedCSS {

	private static Logger log = LoggerFactory.getLogger(SupportedDotifyProperties.class);

	private static final int TOTAL_SUPPORTED_DECLARATIONS = 1;

	private static final TermFactory tf = CSSFactory.getTermFactory();

	private Set<String> supportedCSSproperties;
	private Map<String,CSSProperty> defaultCSSproperties;
	private Map<String,Integer> ordinals;
	private Map<Integer,String> ordinalsRev;

	SupportedDotifyProperties(String prefix) {
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
		return null;
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
		supportedCSSproperties.add(name);
		defaultCSSproperties.put(name, defaultProperty);
	}

	private void setSupportedCSS(String prefix) {
		supportedCSSproperties = new HashSet<String>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSproperties = new HashMap<String,CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		setProperty(prefix + "counter-style", ListStyleType.NONE);
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

