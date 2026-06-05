package org.daisy.pipeline.braille.css;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;

/**
 * Note that <code>CSSStyledText</code> objects are not immutable because {@link SimpleInlineStyle}
 * is mutable (due to the {@link SimpleInlineStyle#removeProperty(String)} and {@link
 * SimpleInlineStyle#iterator()} methods and because the {@link cz.vutbr.web.css.Term} and {@link
 * cz.vutbr.web.css.Declaration} objects are not immutable).
 */
public class CSSStyledText implements Cloneable {
	
	private final String text;
	private final Locale language;
	private Map<String,String> textAttributes;
	private SimpleInlineStyle style;
	
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
		this.style = style;
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
		return style;
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
			clone.style = (SimpleInlineStyle)style.clone();
		if (textAttributes != null)
			clone.textAttributes = new HashMap<String,String>(textAttributes);
		return clone;
	}
	
	@Override
	public String toString() {
		String s = text;
		if (style != null && !style.isEmpty())
			s += "{" + BrailleCssSerializer.getInstance().serializeDeclarationList(style) + "}";
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
		if (this.style != null && !this.style.isEmpty()) {
			if (that.style == null || that.style.isEmpty())
				return false;
			if (!this.style.equals(that.style))
				return false;
		} else if (that.style != null && !that.style.isEmpty())
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
}
