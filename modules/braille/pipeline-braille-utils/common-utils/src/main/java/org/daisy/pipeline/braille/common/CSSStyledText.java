package org.daisy.pipeline.braille.common;

import java.util.HashMap;
import java.util.Map;

import org.daisy.braille.css.InlinedStyle;
import org.daisy.braille.css.InlinedStyle.RuleMainBlock;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.braille.css.SimpleInlineStyle;

import cz.vutbr.web.css.RuleBlock;

public class CSSStyledText implements Cloneable {
		
	private final String text;
	private Style style;
	
	public CSSStyledText(String text, SimpleInlineStyle style) {
		this.text = text;
		this.style = new Style();
		this.style.properties = style;
	}
	
	public CSSStyledText(String text, String style) {
		this.text = text;
		if (style == null)
			this.style = null;
		else
			this.style = parseCSS.apply(style);
	}
	
	public CSSStyledText(String text) {
		this.text = text;
		this.style = null;
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
		return clone;
	}
	
	@Override
	public String toString() {
		if (style == null || style.properties == null || style.properties.isEmpty())
			return text;
		else
			return text + "{" + style.properties + "}";
	}
	
	// TODO: Does this need to be evicted? There is an infinite number of
	// distinct styles due to things like "-dotify-def: tmp_d52242e3"
	private static Memoizing<String,Style> parseCSS = new Memoizing.util.CloningMemoizing<String,Style>() {
		protected Style _apply(String style) {
			InlinedStyle inlinedStyle = new InlinedStyle(style);
			Style s = new Style();
			s.properties = new SimpleInlineStyle(inlinedStyle.getMainStyle());
			for (RuleBlock<?> b : inlinedStyle)
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
