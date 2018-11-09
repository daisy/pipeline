package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextAttribute;

/**
 * Text segment that is "connected" with other segments through Style elements.
 */
public class ConnectedTextSegment extends TextSegment {		
	private final StyledSegmentGroup parentStyle;
	private final int idx;
	private final int width;
	
	public ConnectedTextSegment(String chars, TextProperties tp, StyledSegmentGroup parentStyle) {
		super(chars, tp);
		this.parentStyle = parentStyle;
		idx = parentStyle.add(this);
		width = chars.length();
	}
	
	@Override
	public TextAttribute getTextAttribute() {
		DefaultTextAttribute.Builder b = new DefaultTextAttribute.Builder();
		StyledSegmentGroup s = parentStyle;
		while (s != null) {
			b = new DefaultTextAttribute.Builder(s.getName()).add(b.build(width));
			s = s.getParentStyle();
		}
		return b.build(width);
	}
	
	public TextSegment processAttributes() {
		return parentStyle.processAttributes().getSegmentAt(idx);
	}
}
