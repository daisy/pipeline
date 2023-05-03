package org.daisy.braille.css;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.SourceLocator;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.csskit.TermIdentImpl;
import cz.vutbr.web.domassign.SingleMapNodeData.Quadruple;

public class PropertyValue extends AbstractList<Term<?>> implements Cloneable, Declaration {
	
	private final String propertyName;
	protected Quadruple propertyValue; // not final for clone()
	private CSSProperty property; // not final for clone()
	private Term<?> value; // not final for clone()
	private Term<?> declaration; // not final for clone()
	private Declaration sourceDeclaration; // not final for clone()
	
	public PropertyValue(String propertyName,
	                     final CSSProperty property,
	                     final Term<?> value,
	                     final Declaration sourceDeclaration,
	                     SupportedCSS css) {
		this(propertyName, new Quadruple(css, propertyName) {{
			curProp = property;
			curValue = value;
			curSource = sourceDeclaration;
		}});
	}
	
	public PropertyValue(PropertyValue propertyValue) {
		this(propertyValue.propertyName, propertyValue.propertyValue);
	}
	
	PropertyValue(String propertyName, Quadruple propertyValue) {
		if (propertyValue.isEmpty())
			throw new IllegalArgumentException();
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		init();
	}

	// called from constructor and from clone()
	private void init() {
		property = propertyValue.getProperty(true);
		value = propertyValue.getValue(true);
		sourceDeclaration = propertyValue.getSourceDeclaration(true);
		if (value != null)
			declaration = value;
		else if (sourceDeclaration != null && sourceDeclaration.getProperty().equals(propertyName))
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
	
	/**
	 * Returns <code>null</code> if the property is unknown.
	 */
	public PropertyValue getDefault() {
		Quadruple q = propertyValue.getDefault();
		if (q == propertyValue)
			return this;
		else if (q.isEmpty())
			// unknown property
			return null;
		else
			return new PropertyValue(propertyName, q);
	}
	
	private boolean concretizedInherit = false;
	private boolean concretizedInitial = false;
	
	/**
	 * Does not mutate the object. Returns a new object if needed.
	 */
	public PropertyValue concretize(boolean concretizeInherit, boolean concretizeInitial) {
		if ((concretizedInherit || !concretizeInherit) && (concretizedInitial || !concretizeInitial))
			return this;
		else {
			Quadruple q = (Quadruple)propertyValue.clone();
			q.concretize(concretizeInherit, concretizeInitial);
			PropertyValue concretized = new PropertyValue(propertyName, q);
			concretized.concretizedInherit = concretizeInherit;
			concretized.concretizedInitial = concretizeInitial;
			return concretized;
		}
	}

	private boolean inherited = false;
	
	/**
	 * Does not mutate the object. Returns a new object if needed.
	 */
	public PropertyValue inheritFrom(PropertyValue parent) {
		if (inherited)
			throw new UnsupportedOperationException("Can not inherit from more than one parent style");
		else if (concretizedInherit)
			throw new UnsupportedOperationException("Can not inherit from a parent style: 'inherit' values were already concretized.");
		Quadruple q = (Quadruple)propertyValue.clone();
		q.inheritFrom(parent.concretize(true, false).propertyValue);
		q.concretize(true, false);
		PropertyValue inherited = new PropertyValue(propertyName, q);
		inherited.inherited = inherited.concretizedInherit = true;
		return inherited;
	}
	
	/* ===== Declaration ===== */
	
	@Override
	public String getProperty() {
		return propertyName;
	}
	
	@Override
	public boolean isImportant() {
		return sourceDeclaration != null ? sourceDeclaration.isImportant() : false;
	}
	
	@Override
	public SourceLocator getSource() {
		return sourceDeclaration != null ? sourceDeclaration.getSource() : null;
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
		clone.propertyValue = (Quadruple)propertyValue.clone();
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
	
	public static PropertyValue parse(String property, String value) {
		return parse(SimpleInlineStyle.parserFactory.parseDeclaration(property + ":" + value));
	}
	
	public static PropertyValue parse(final Declaration declaration) {
		final Map<String,CSSProperty> properties = new HashMap<String,CSSProperty>();
		final Map<String,Term<?>> terms = new HashMap<String,Term<?>>();
		if (!SimpleInlineStyle.cssInstance.parseDeclaration(declaration, properties, terms))
			return null;
		final String propertyName = declaration.getProperty();
		return new PropertyValue(
			propertyName,
			properties.get(propertyName),
			terms.get(propertyName),
			declaration,
			SimpleInlineStyle.cssInstance);
	}
}
