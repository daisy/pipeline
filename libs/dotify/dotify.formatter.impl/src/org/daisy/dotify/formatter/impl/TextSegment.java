package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.TextAttribute;

class TextSegment implements Segment {
	private final String chars;
	private final TextProperties tp;
	private final TextAttribute ta;
	
	public TextSegment(String chars, TextProperties tp) {
		this(chars, tp, null);
	}

	public TextSegment(String chars, TextProperties tp, TextAttribute ta) {
		this.chars = chars;
		this.tp = tp;
		this.ta = ta;
	}
	
	public String getText() {
		return chars;
	}

	public TextProperties getTextProperties() {
		return tp;
	}

	public TextAttribute getTextAttribute() {
		return ta;
	}
	
	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Text;
	}

}
