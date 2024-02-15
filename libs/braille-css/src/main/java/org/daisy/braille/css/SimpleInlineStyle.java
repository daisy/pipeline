package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.domassign.SingleMapNodeData;

public class SimpleInlineStyle extends SingleMapNodeData implements NodeData, Cloneable, Iterable<PropertyValue> {
	
	final static SupportedBrailleCSS cssInstance = new SupportedBrailleCSS(true, false);
	final static BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory();
	
	public static final SimpleInlineStyle EMPTY = new SimpleInlineStyle((List<Declaration>)null);

	private SupportedBrailleCSS css;

	public SimpleInlineStyle(String style) {
		this(style, null);
	}
	
	/**
	 * @param parentStyle if a parent style is provided (not <code>null</code>), all <a
	 *                    href="https://www.w3.org/TR/CSS2/cascade.html#inheritance">inherited</a>
	 *                    properties are included, and all "<a
	 *                    href="https://www.w3.org/TR/CSS2/cascade.html#value-def-inherit
	 *                    >inherit</a>" values are concretized. If no parent style is provided,
	 *                    nothing gets inherited or concretized.
	 */
	public SimpleInlineStyle(String style, SimpleInlineStyle parentStyle) {
		this(
			(style != null && !"".equals(style)) ? parserFactory.parseSimpleInlineStyle(style) : (List<Declaration>)null,
			parentStyle);
	}
	
	public SimpleInlineStyle(Iterable<? extends Declaration> declarations) {
		this(declarations, null);
	}
	
	public SimpleInlineStyle(Iterable<? extends Declaration> declarations, SimpleInlineStyle parentStyle) {
		this(declarations, parentStyle, null);
	}
	
	/**
	 * @param css {@link SupportedBrailleCSS} to be used to process declarations that are not {@link
	 *            PropertyValue} instances. If non-{@code null}, assert that declarations that are
	 *            {@link PropertyValue} were created using the given {@link SupportedBrailleCSS}. If
	 *            {@code null}, assert that all {@link PropertyValue} declarations were created with
	 *            the same {@link SupportedBrailleCSS}.
	 * @throws IllegalArgumentException if a parent style or declaration is provided that is not
	 *                                  compatible with the provided {@code SupportedBrailleCSS}
	 */
	public SimpleInlineStyle(Iterable<? extends Declaration> declarations, SimpleInlineStyle parentStyle, SupportedBrailleCSS css) {
		super(css == null ? cssInstance : css,
		      css == null ? cssInstance : css);
		if (declarations != null) {
			for (Declaration d : declarations) {
				if (!(d instanceof PropertyValue)) {
					if (css == null)
						css = cssInstance;
					super.push(d);
				}
			}
			for (Declaration d : declarations) {
				if (d instanceof PropertyValue) {
					PropertyValue v = (PropertyValue)d;
					if (css == null)
						css = v.css;
					if (v.css.equals(css))
						map.put(v.getProperty(), v.propertyValue);
					else 
						throw new IllegalArgumentException();
				}
			}
		}
		if (parentStyle != null) {
			if (declarations != null)
				for (Declaration d : declarations)
					for (PropertyValue dd : parentStyle) {
						if (!css.equals(dd.css))
							throw new IllegalArgumentException();
						break; }
			super.inheritFrom(parentStyle.concretize());
			super.concretize(true, true);
			inherited = concretizedInherit = concretizedInitial = true;
			if (css == null)
				css = parentStyle.css;
		} else {
			super.concretize(false, true);
			concretizedInitial = true;
		}
		this.css = css;
	}

	public Term<?> getValue(String name) {
		return getValue(name, true);
	}
	
	public void removeProperty(String name) {
		map.remove(name);
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public int size() {
		return map.size();
	}
	
	public Iterator<PropertyValue> iterator() {
		return new Iterator<PropertyValue>() {
			Iterator<String> props = SimpleInlineStyle.this.map.keySet().iterator();
			public boolean hasNext() {
				return props.hasNext();
			}
			public PropertyValue next() {
				return SimpleInlineStyle.this.get(props.next());
			}
			public void remove() {
				props.remove();
			}
		};
	}
	
	public PropertyValue get(String property) {
		Quadruple q = map.get(property);
		if (q == null)
			return null;
		if (css == null)
			throw new IllegalStateException(); // can not happen
		return new PropertyValue(property, q, css);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			if (sb.length() > 0)
				sb.append("; ");
			sb.append(key).append(": ");
			Term<?> value = getValue(key);
			if (value != null)
				sb.append(value);
			else
				sb.append(getProperty(key).toString()); }
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleInlineStyle) {
			SimpleInlineStyle that = (SimpleInlineStyle)other;
			Set<String> keys = map.keySet();
			if (!keys.equals(that.map.keySet()))
				return false;
			for (String key : keys) {
				Term<?> value = getValue(key);
				if ((value == null && that.getValue(key) != null)
				    || (value != null && !value.equals(that.getValue(key))))
					return false;
				CSSProperty prop = getProperty(key);
				if ((prop == null && that.getProperty(key) != null)
				    || (prop != null && !prop.equals(that.getProperty(key))))
					return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean concretizedInherit = false;
	private boolean concretizedInitial = false;

	@Override
	public SimpleInlineStyle concretize() {
		return (SimpleInlineStyle)super.concretize();
	}

	@Override
	public SimpleInlineStyle concretize(boolean concretizeInherit, boolean concretizeInitial) {
		if ((concretizedInherit || !concretizeInherit) && (concretizedInitial || !concretizeInitial))
			return this;
		else {
			SimpleInlineStyle copy = (SimpleInlineStyle)clone();
			copy.noCopyConcretize(concretizeInherit, concretizeInitial);
			return copy;
		}
	}
	
	private void noCopyConcretize(boolean concretizeInherit, boolean concretizeInitial) {
		super.concretize(concretizeInherit, concretizeInitial);
		concretizedInherit = concretizeInherit;
		concretizedInitial = concretizeInitial;
	}

	private boolean inherited = false;
	
	@Override
	public SimpleInlineStyle inheritFrom(NodeData parent) throws ClassCastException {
		if (inherited)
			throw new UnsupportedOperationException("Can not inherit from more than one parent style");
		else if (concretizedInherit)
			throw new UnsupportedOperationException("Can not inherit from a parent style: 'inherit' values were already concretized.");
		else if (!(parent instanceof SimpleInlineStyle))
			throw new UnsupportedOperationException("Can only inherit from another SimpleInlineStyle");
		else {
			SimpleInlineStyle copy = (SimpleInlineStyle)clone();
			copy.noCopyInheritFrom(((SimpleInlineStyle)parent).concretize());
			copy.noCopyConcretize(true, false);
			if (copy.css == null)
				copy.css = ((SimpleInlineStyle)parent).css;
			return copy;
		}
	}

	private void noCopyInheritFrom(NodeData parent) {
		super.inheritFrom(parent);
		inherited = true;
	}

	@Override
	public NodeData push(Declaration d) {
		throw new UnsupportedOperationException();
	}
}
