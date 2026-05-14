package org.daisy.pipeline.braille.css;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.Color;
import cz.vutbr.web.css.CSSProperty.FontStyle;
import cz.vutbr.web.css.CSSProperty.FontWeight;
import cz.vutbr.web.css.CSSProperty.TextDecoration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * @author bert
 */
public class SupportedPrintCSS implements SupportedCSS {
	
	private static Logger log = LoggerFactory.getLogger(SupportedPrintCSS.class);
	
	private static final int TOTAL_SUPPORTED_DECLARATIONS = 4;
	
	private static final TermFactory tf = CSSFactory.getTermFactory(); // cz.vutbr.web.csskit.TermFactoryImpl
	
	private static final Term<?> DEFAULT_UA_COLOR = tf.createColor("#000000");
	
	private Map<String, CSSProperty> defaultCSSproperties;
	private Map<String, Term<?>> defaultCSSvalues;
	
	private Map<String, Integer> ordinals;
	private Map<Integer, String> ordinalsRev;
	
	private Set<String> properties;
	
	private static SupportedPrintCSS instance;
	
	public final static SupportedPrintCSS getInstance() {
		if (instance == null)
			instance = new SupportedPrintCSS();
		return instance;
	}
	
	private SupportedPrintCSS() {
		this.setSupportedCSS();
		this.setOridinals();
	}
	
	@Override
	public boolean isSupportedMedia(String media) {
		if (media == null)
			return false;
		return media.toLowerCase().equals("print");
	}
	
	@Override
	public final boolean isSupportedCSSProperty(String property) {
		return properties.contains(property);
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
	
	private void setSupportedCSS() {
		
		Map<String, CSSProperty> props = new HashMap<String, CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		Map<String, Term<?>> values = new HashMap<String, Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		
		properties = new HashSet<String>();
		
		// text type
		props.put("color", Color.color);
		values.put("color", DEFAULT_UA_COLOR);
		properties.add("color");
		props.put("font-style", FontStyle.NORMAL);
		properties.add("font-style");
		props.put("font-weight", FontWeight.NORMAL);
		properties.add("font-weight");
		props.put("text-decoration", TextDecoration.NONE);
		properties.add("text-decoration");
		
		this.defaultCSSproperties = props;
		this.defaultCSSvalues = values;
		
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
