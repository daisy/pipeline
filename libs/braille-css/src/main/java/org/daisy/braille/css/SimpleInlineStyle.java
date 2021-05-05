package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.domassign.SingleMapNodeData;

public class SimpleInlineStyle extends SingleMapNodeData implements NodeData, Cloneable, Iterable<PropertyValue> {
	
	private final static SupportedCSS cssInstance = new SupportedBrailleCSS();
	private static BrailleCSSDeclarationTransformer transformerInstance; static {
		// SupportedCSS injected via CSSFactory in DeclarationTransformer.<init>
		CSSFactory.registerSupportedCSS(cssInstance);
		transformerInstance = new BrailleCSSDeclarationTransformer(); }
	private final static BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory();
	
	public static final SimpleInlineStyle EMPTY = new SimpleInlineStyle((List<Declaration>)null);

	public SimpleInlineStyle(String style) {
		this(style, null);
	}
	
	public SimpleInlineStyle(String style, SimpleInlineStyle parentStyle) {
		this(
			(style != null && !"".equals(style)) ? parserFactory.parseSimpleInlineStyle(style) : (List<Declaration>)null,
			parentStyle);
	}
	
	public SimpleInlineStyle(List<Declaration> declarations) {
		this(declarations, null);
	}
	
	public SimpleInlineStyle(List<Declaration> declarations, SimpleInlineStyle parentStyle) {
		super();
		transformer = transformerInstance;
		css = cssInstance;
		if (declarations != null)
			for (Declaration d : declarations) {
				// SupportedCSS injected via CSSFactory in Repeater.assignDefaults, Variator.assignDefaults
				CSSFactory.registerSupportedCSS(css);
				push(d); }
		if (parentStyle != null)
			inheritFrom(parentStyle);
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
				String prop = props.next();
				return new PropertyValue(
					SimpleInlineStyle.this.getProperty(prop),
					SimpleInlineStyle.this.getValue(prop));
			}
			public void remove() {
				props.remove();
			}
		};
	}
	
	public PropertyValue get(String property) {
		CSSProperty p = getProperty(property);
		if (p == null)
			return null;
		return new PropertyValue(p, getValue(property));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		for (String key: keys) {
			if (sb.length() > 0)
				sb.append("; ");
			sb.append(key).append(": ");
			Term<?> value = getValue(key);
			if (value != null)
				sb.append(value);
			else
				sb.append(getProperty(key)); }
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleInlineStyle)
			return map.equals(((SimpleInlineStyle)other).map);
		return false;
	}
}
