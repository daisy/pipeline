package org.daisy.braille.css;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.SourceLocator;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.csskit.TermIdentImpl;

public class PropertyValue extends AbstractList<Term<?>> implements Cloneable, Declaration {
	
	private final String propertyName;
	private final CSSProperty property;
	private Term<?> value; // not final for clone()
	private Term<?> declaration; // not final for clone()
	private final Declaration sourceDeclaration;
	
	PropertyValue(String propertyName, final CSSProperty property, Term<?> value, Declaration sourceDeclaration) {
		this.propertyName = propertyName;
		this.property = property;
		this.value = value;
		this.sourceDeclaration = sourceDeclaration;
		init();
	}

	// called from constructor and from clone()
	private void init() {
		if (value != null)
			declaration = value;
		else if (sourceDeclaration.getProperty().equals(propertyName))
			declaration = sourceDeclaration.get(0); // assuming sourceDeclaration is a single TermIdent
		else
			// repeater and/or variator was applied
			declaration = new TermIdentImpl() {{
				value = property.toString();
				operator = null; }
					@Override public TermIdent setValue(String value) { throw new UnsupportedOperationException("Unmodifiable"); }
					@Override public TermIdent setOperator(Operator operator) { throw new UnsupportedOperationException("Unmodifiable"); }
				};
	}
	
	public CSSProperty getCSSProperty() {
		return property;
	}
	
	/**
	 * Returned object is not immutable.
	 */
	public Term<?> getValue() {
		return value;
	}

	/**
	 * Returned object can be assumed to be immutable.
	 */
	public Declaration getSourceDeclaration() {
		return sourceDeclaration;
	}
	
	/* ===== Declaration ===== */
	
	@Override
	public String getProperty() {
		return propertyName;
	}
	
	@Override
	public boolean isImportant() {
		return sourceDeclaration.isImportant();
	}
	
	@Override
	public SourceLocator getSource() {
		return sourceDeclaration.getSource();
	}
	
	@Override public void setProperty(String p) { throw new UnsupportedOperationException("immutable"); }
	@Override public void setImportant(boolean i) { throw new UnsupportedOperationException("immutable"); }
	@Override public void setSource(SourceLocator sl) { throw new UnsupportedOperationException("immutable"); }
	
	// List<Term<?>>
	
	@Override
	public int size() {
		return 1;
	}
	
	@Override
	public Term<?> get(int i) {
		return i == 0 ? declaration : null;
	}
	
	// Rule<Term<?>>
	
	@Override
	public List<Term<?>> asList() {
		return this;
	}
	
	@Override public Rule<Term<?>> replaceAll(List<Term<?>> replacement) { throw new UnsupportedOperationException("immutable"); }
	@Override public Rule<Term<?>> unlock() { throw new UnsupportedOperationException("immutable"); }
	
	// PrettyOutput
	
	@Override
	public String toString(int depth) {
		return toString();
	}
	
	// Comparable<Declaration>
	
	@Override
	public int compareTo(Declaration o) {
		if (this.isImportant() && !o.isImportant())
			return 1;
		else if (o.isImportant() && !this.isImportant())
			return -1;
		return 0;
	}
	
	/* ============================= */
	
	@Override
	public Object clone() {
		PropertyValue clone; {
			try {
				clone = (PropertyValue)super.clone(); }
			catch (CloneNotSupportedException e) {
				throw new InternalError("coding error"); }}
		if (value != null)
			clone.value = (Term<?>)value.clone();
		clone.init();
		return clone;
	}
	
	@Override
	public String toString() {
		if (value != null)
			return propertyName + ": " + value.toString();
		else
			return propertyName + ": " + property.toString();
	}
	
	/* ============================= */
	
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
		String propertyName = declaration.getProperty();
		return new PropertyValue(propertyName, properties.get(propertyName), terms.get(propertyName), declaration);
	}
}
