package org.daisy.pipeline.braille.common;

import java.util.HashMap;
import java.util.Map;

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
	private Map<String,String> textAttributes;
	private Style style;
	
	public CSSStyledText(String text, SimpleInlineStyle style) {
		this(text, style, null);
	}
	
	public CSSStyledText(String text, SimpleInlineStyle style, Map<String,String> textAttributes) {
		this.text = text;
		this.style = new Style();
		this.style.properties = style;
		this.textAttributes = textAttributes;
	}
	
	public CSSStyledText(String text, String style) {
		this(text, style, null);
	}
	
	public CSSStyledText(String text, String style, Map<String,String> textAttributes) {
		this.text = text;
		if (style == null)
			this.style = null;
		else
			this.style = parseCSS.apply(style);
		this.textAttributes = textAttributes;
	}
	
	public CSSStyledText(String text) {
		this.text = text;
		this.style = null;
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
	
	public RuleTextTransform getTextTransformDefinition(String name) {
		if (style == null || style.textTransformDefs == null)
			return null;
		else
			return style.textTransformDefs.get(name);
	}
	
	public Map<String,String> getTextAttributes() {
		return textAttributes;
	}
	
	@Override
	public Object clone() {
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
		return s;
	}
	
	// TODO: Does this need to be evicted? There is an infinite number of
	// distinct styles due to things like "-dotify-def: tmp_d52242e3"
	private static Memoizing<String,Style> parseCSS = new Memoizing.util.CloningMemoizing<String,Style>() {
		protected Style _apply(String style) {
			InlineStyle inlineStyle = new InlineStyle(style);
			Style s = new Style();
			s.properties = new SimpleInlineStyle(inlineStyle.getMainStyle());
			for (RuleBlock<?> b : inlineStyle)
				if (b instanceof RuleMainBlock) {} // already handled
				else if (b instanceof RuleTextTransform) {
					if (s.textTransformDefs == null)
						s.textTransformDefs = new HashMap<String,RuleTextTransform>();
					RuleTextTransform def = (RuleTextTransform)b;
					s.textTransformDefs.put(def.getName(), def);
				} else
					throw new RuntimeException("Unexpected style: " + b);
			return s;
		}
	};
	
	private static class Style implements Cloneable {
		SimpleInlineStyle properties;
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
	}
}
