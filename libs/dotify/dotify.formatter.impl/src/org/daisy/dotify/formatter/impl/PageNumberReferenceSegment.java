package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.NumeralStyle;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextAttribute;

class PageNumberReferenceSegment extends PageNumberReference implements Segment {
	
	private final String[] textStyle;
	
	public PageNumberReferenceSegment(String refid, NumeralStyle style) {
		this(refid, style, null);
	}
	
	/**
	 * @param style Array of styles to apply (from outer to inner).
	 */
	public PageNumberReferenceSegment(String refid, NumeralStyle style, String[] textStyle) {
		super(refid, style);
		this.textStyle = textStyle;
	}

	/**
	 * @param width The width of the evaluated expression.
	 */
	public TextAttribute getTextAttribute(int width) {
		if (textStyle == null || textStyle.length == 0) {
			return null;
		} else {
			TextAttribute a = new DefaultTextAttribute.Builder(textStyle[0]).build(width);
			for (int i = 1; i < textStyle.length; i++) {
				a = new DefaultTextAttribute.Builder(textStyle[i]).add(a).build(width);
			}
			return a;
		}
	}
	
	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Reference;
	}

}