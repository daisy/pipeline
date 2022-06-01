package org.daisy.braille.css;

import java.util.HashMap;
import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
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
	
	private static BrailleCSSDeclarationTransformer transformerInstance
		= new BrailleCSSDeclarationTransformer(new SupportedBrailleCSS(true, false));
	private final static BrailleCSSParserFactory parserFactoryInstance = new BrailleCSSParserFactory();
	
	public static PropertyValue parse(String property, String value) {
		return parse(parserFactoryInstance.parseDeclaration(property + ":" + value));
	}
	
	public static PropertyValue parse(Declaration declaration) {
		Map<String,CSSProperty> properties = new HashMap<String,CSSProperty>();
		Map<String,Term<?>> terms = new HashMap<String,Term<?>>();
		if (!transformerInstance.parseDeclaration(declaration, properties, terms))
			return null;
		String property = declaration.getProperty();
		return new PropertyValue(properties.get(property), terms.get(property));
	}
}
