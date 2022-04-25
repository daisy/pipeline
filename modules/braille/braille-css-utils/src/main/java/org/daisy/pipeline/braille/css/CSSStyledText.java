package org.daisy.pipeline.braille.css;

import java.util.function.Function;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.InlineStyle.RuleMainBlock;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.SimpleInlineStyle;

import cz.vutbr.web.css.RuleBlock;

/**
 * Note that <code>CSSStyledText</code> objects are not immutable because {@link SimpleInlineStyle}
 * is mutable (due to {@link SimpleInlineStyle#removeProperty(String)} and {@link
 * SimpleInlineStyle#iterator()} methods).
 */
public class CSSStyledText implements Cloneable {
	
	private final String text;
	private final Locale language;
	private Map<String,String> textAttributes;
	private Style style;
	
	private static final Function<String,Style> parseCSS = memoize(Style::parse);
	
	public CSSStyledText(String text, SimpleInlineStyle style) {
		this(text, style, null, null);
	}
	
	public CSSStyledText(String text, SimpleInlineStyle style, Locale language) {
		this(text, style, language, null);
	}
	
	public CSSStyledText(String text, SimpleInlineStyle style, Map<String,String> textAttributes) {
		this(text, style, null, textAttributes);
	}
	
	public CSSStyledText(String text, SimpleInlineStyle style, Locale language, Map<String,String> textAttributes) {
		this.text = text;
		this.style = new Style();
		this.style.properties = style;
		this.language = language;
		this.textAttributes = textAttributes;
	}
	
	public CSSStyledText(String text, String style) {
		this(text, style, null, null);
	}
	
	public CSSStyledText(String text, String style, Locale language) {
		this(text, style, language, null);
	}
	
	public CSSStyledText(String text, String style, Map<String,String> textAttributes) {
		this(text, style, null, textAttributes);
	}
	
	public CSSStyledText(String text, String style, Locale language, Map<String,String> textAttributes) {
		this.text = text;
		if (style == null)
			this.style = null;
		else
			this.style = parseCSS.apply(style);
		this.language = language;
		this.textAttributes = textAttributes;
	}
	
	public CSSStyledText(String text) {
		this.text = text;
		this.style = null;
		this.language = null;
		this.textAttributes = null;
	}
	
	public String getText() {
		return text;
	}
	
	public SimpleInlineStyle getStyle() {
		if (style == null)
			return null;
		else
			return style.properties;
	}
	
	public RuleTextTransform getDefaultTextTransformDefinition() {
		if (style == null)
			return null;
		else
			return style.defaultTextTransformDef;
	}
	
	public RuleTextTransform getTextTransformDefinition(String name) {
		if (style == null || style.textTransformDefs == null)
			return null;
		else
			return style.textTransformDefs.get(name);
	}
	
	public Locale getLanguage() {
		return language;
	}
	
	public Map<String,String> getTextAttributes() {
		return textAttributes;
	}
	
	@Override
	public CSSStyledText clone() {
		CSSStyledText clone; {
			try {
				clone = (CSSStyledText)super.clone();
			} catch (CloneNotSupportedException e) {
				throw new InternalError("coding error");
			}
		}
		if (style != null)
			clone.style = (Style)style.clone();
		if (textAttributes != null)
			clone.textAttributes = new HashMap<String,String>(textAttributes);
		return clone;
	}
	
	@Override
	public String toString() {
		String s = text;
		if (style != null && style.properties != null && !style.properties.isEmpty())
			s += "{" + style.properties + "}";
		if (language != null && !"und".equals(language.toLanguageTag()))
			s += "{" + language.toLanguageTag() + "}";
		return s;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof CSSStyledText))
			return false;
		CSSStyledText that = (CSSStyledText)other;
		if (this.text != null) {
			if (that.text == null)
				return false;
			if (!this.text.equals(that.text))
				return false;
		} else if (that.text != null)
			return false;
		if (this.textAttributes != null && !this.textAttributes.isEmpty()) {
			if (that.textAttributes == null || that.textAttributes.isEmpty())
				return false;
			if (!this.textAttributes.equals(that.textAttributes))
				return false;
		} else if (that.textAttributes != null && !that.textAttributes.isEmpty())
			return false;
		if (this.style != null && this.style.properties != null && !this.style.properties.isEmpty()) {
			if (that.style == null || that.style.properties == null || that.style.properties.isEmpty())
				return false;
			if (!this.style.equals(that.style))
				return false;
		} else if (that.style != null && that.style.properties != null && !that.style.properties.isEmpty())
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash + (text == null ? 0 : text.hashCode());
		hash = prime * hash + (textAttributes == null ? 0 : textAttributes.hashCode());
		hash = prime * hash + (style == null ? 0 : style.hashCode());
		return hash;
	}

	private static class Style implements Cloneable {

		SimpleInlineStyle properties;
		RuleTextTransform defaultTextTransformDef;
		Map<String,RuleTextTransform> textTransformDefs;

		@Override
		public Object clone() {
			Style clone; {
				try {
					clone = (Style)super.clone();
				} catch (CloneNotSupportedException e) {
					throw new InternalError("coding error");
				}
			}
			if (properties != null)
				clone.properties = (SimpleInlineStyle)properties.clone();
			return clone;
		}

		@Override
		// FIXME: don't ignore text-transform defs
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof Style))
				return false;
			Style that = (Style)other;
			if (this.properties != null) {
				if (that.properties == null)
					return false;
				if (!this.properties.equals(that.properties))
					return false;
			} else if (that.properties != null)
				return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 1;
			hash = prime * hash + (properties == null ? 0 : properties.hashCode());
			return hash;
		}
		
		public static Style parse(String style) {
			InlineStyle inlineStyle = new InlineStyle(style);
			Style s = new Style();
			s.properties = new SimpleInlineStyle(inlineStyle.getMainStyle());
			for (RuleBlock<?> b : inlineStyle)
				if (b instanceof RuleMainBlock) {} // already handled
				else if (b instanceof RuleTextTransform) {
					RuleTextTransform def = (RuleTextTransform)b;
					String name = def.getName();
					if (name != null) {
						if (s.textTransformDefs == null)
							s.textTransformDefs = new HashMap<String,RuleTextTransform>();
						s.textTransformDefs.put(name, def);
					} else
						s.defaultTextTransformDef = def;
				} else
					throw new RuntimeException("Unexpected style: " + b);
			return s;
		}
	}

	private static <K,V extends Cloneable> Function<K,V> memoize(Function<K,V> function) {
		Map<K,V> cache = new HashMap<K,V>();
		return new Function<K,V>() {
			public V apply(K key) {
				V value;
				if (cache.containsKey(key))
					value = cache.get(key);
				else {
					value = function.apply(key);
					if (value != null)
						cache.put(key, value); }
				if (value == null)
					return null;
				else {
					try {
						return (V)value.getClass().getMethod("clone").invoke(value); }
					catch (IllegalAccessException
					       | IllegalArgumentException
					       | InvocationTargetException
					       | NoSuchMethodException
					       | SecurityException e) {
						throw new RuntimeException("Could not invoke clone() method", e);
					}
				}
			}
		};
	}
}
