package org.daisy.pipeline.braille.common;

import org.daisy.braille.css.SimpleInlineStyle;

public class CSSStyledText implements Cloneable {
		
	private final String text;
	private SimpleInlineStyle style;
		
	public CSSStyledText(String text, SimpleInlineStyle style) {
		this.text = text;
		this.style = style;
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
		return style;
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
			clone.style = (SimpleInlineStyle)style.clone();
		return clone;
	}
	
	@Override
	public String toString() {
		if (style == null || style.isEmpty())
			return text;
		else
			return text + "{" + style + "}";
	}
	
	// TODO: Does this need to be evicted? There is an infinite number of
	// distinct styles due to things like "-dotify-def: tmp_d52242e3"
	private static Memoizing<String,SimpleInlineStyle> parseCSS = new Memoizing.util.CloningMemoizing<String,SimpleInlineStyle>() {
		protected SimpleInlineStyle _apply(String style) {
			return new SimpleInlineStyle(style);
		}
	};
}
