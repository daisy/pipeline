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

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.BrailleCSSProperty.TextIndent;

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
		log.debug("Totally found {} parsing methods", map.size());
		return map;
	}

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		String property = d.getProperty().toLowerCase();
		if (!property.startsWith(prefix))
			property = prefix + property;
		if (!css.isSupportedCSSProperty(property))
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
