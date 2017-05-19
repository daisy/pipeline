package org.daisy.braille.css;

import java.util.HashMap;
import java.util.Map;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;

public class PropertyValue implements Cloneable {
	
	private CSSProperty property;
	private Term<?> value;
	
	PropertyValue(CSSProperty property, Term<?> value) {
		this.property = property;
		this.value = value;
	}
	
	public CSSProperty getProperty() {
		return property;
	}
	
	public Term<?> getValue() {
		return value;
	}
	
	@Override
	public Object clone() {
		PropertyValue clone; {
			try {
				clone = (PropertyValue)super.clone(); }
			catch (CloneNotSupportedException e) {
				throw new InternalError("coding error"); }}
		clone.property = property;
		if (value != null)
			clone.value = (Term<?>)value.clone();
		return clone;
	}
	
	@Override
	public String toString() {
		if (value != null)
			return value.toString();
		else
			return property.toString();
	}
	
	private final static SupportedCSS cssInstance = new SupportedBrailleCSS(true, false);
	private static BrailleCSSDeclarationTransformer transformerInstance; static {
		// SupportedCSS injected via CSSFactory in DeclarationTransformer.<init>
		CSSFactory.registerSupportedCSS(cssInstance);
		transformerInstance = new BrailleCSSDeclarationTransformer(); }
	private final static BrailleCSSParserFactory parserFactoryInstance = new BrailleCSSParserFactory();
	
	
	public static PropertyValue parse(String property, String value) {
		Declaration d = parserFactoryInstance.parseDeclaration(property + ":" + value);
		Map<String,CSSProperty> properties = new HashMap<String,CSSProperty>();
		Map<String,Term<?>> terms = new HashMap<String,Term<?>>();
		// SupportedCSS injected via CSSFactory in Repeater.assignDefaults, Variator.assignDefaults
		CSSFactory.registerSupportedCSS(cssInstance);
		if (!transformerInstance.parseDeclaration(d, properties, terms))
			return null;
		return new PropertyValue(properties.get(property), terms.get(property));
	}
}
