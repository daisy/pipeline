import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.TextIndent;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.VendorAtRule;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "MockCSSExtension",
	service = {
		BrailleCSSExtension.class
	}
)
public class MockCSSExtension extends BrailleCSSExtension {

	private final static String prefix = "-foo-";

	public MockCSSExtension() {
		// SupportedFooProperties is defined at the bottom of this file
		// it is a separate class because we need an argument to pass to the constructor of BrailleCSSExtension
		super(new SupportedFooProperties(prefix));
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
			if ((prefix + "marker-indicator").equals(funcName)) {
				if (f.size() != 2)
					return false;
				if (!(f.get(0) instanceof TermIdent))
					return false;
				if (!(f.get(1) instanceof TermString))
					return false;
			} else
				return false;
			list.add(term);
			return true;
		}
		return false;
	}

	@Override
	public PseudoElement createPseudoElement(String name) throws IllegalArgumentException {
		if ((prefix + "on-volume-start").equals(name) ||
		    (":" + prefix + "alternate-scenario").equals(name))
			return new PseudoElementImpl(name);
		throw new IllegalArgumentException("Unknown pseudo-element ::" + name);
	}

	@Override
	public PseudoElement createPseudoElementFunction(String name, String... args) throws IllegalArgumentException {
		if ((":" + prefix + "alternate-scenario").equals(name) && args.length == 1)
			return new PseudoElementImpl(name, args);
		throw new IllegalArgumentException("Unknown pseudo-element ::" + name);
	}

	@Override
	public VendorAtRule<? extends Rule<?>> createAtRule(String name, List<Rule<?>> content) throws IllegalArgumentException {
		if ((prefix + "volume-transition").equals(name)
		    || "any-interrupted".equals(name))
			return new VendorAtRule<Rule<?>>(name, content);
		throw new IllegalArgumentException("Unknown at-rule @" + name);
	}

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	private Map<String,Method> findParsingMethods() {
		Map<String,Method> map = new HashMap<String,Method>(css.getTotalProperties(), 1.0f);
		for (String property : css.getDefinedPropertyNames()) {
			try {
				Method m = MockCSSExtension.class.getDeclaredMethod(
					camelCase("process-" + property.substring(prefix.length())),
					Declaration.class, Map.class, Map.class);
				map.put(property, m);
			} catch (Exception e) {
				log.debug("Unable to find method for property {}.", property); // should not happen
			}
		}
		for (String property : new String[]{"display"}) {
			try {
				Method m = MockCSSExtension.class.getDeclaredMethod(
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
		if (!(css.isSupportedCSSProperty(property) || "display".equals(property))) {
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
	private boolean processRightTextIndent(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return genericOneIdentOrInteger(TextIndent.class, TextIndent.integer, false, d, properties, values);
	}

	@SuppressWarnings("unused")
	private boolean processDisplay(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		if (d.size() != 1)
			return false;
		Term t = d.get(0);
		if (t instanceof TermIdent) {
			String display = ((TermIdent)t).getValue();
			if ((prefix + "bar").equals(display)) {
				String prop = d.getProperty();
				properties.put(prop, Display.custom);
				values.put(prop, t);
				return true;
			}
		}
		return false;
	}
}

class SupportedFooProperties implements SupportedCSS {

	private static Logger log = LoggerFactory.getLogger(SupportedFooProperties.class);

	private static final int TOTAL_SUPPORTED_DECLARATIONS = 1;

	private static final TermFactory tf = CSSFactory.getTermFactory();

	private static final Term<?> DEFAULT_UA_TEXT_IDENT = tf.createInteger(0);

	private Set<String> supportedCSSproperties;
	private Map<String,CSSProperty> defaultCSSproperties;
	private Map<String,Term<?>> defaultCSSvalues;
	private Map<String,Integer> ordinals;
	private Map<Integer,String> ordinalsRev;

	SupportedFooProperties(String prefix) {
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
		defaultCSSvalues = new HashMap<String,Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		setProperty(prefix + "right-text-indent", TextIndent.integer, DEFAULT_UA_TEXT_IDENT);
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
