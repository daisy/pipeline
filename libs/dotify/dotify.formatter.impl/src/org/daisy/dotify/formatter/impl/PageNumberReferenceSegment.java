package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.NumeralStyle;

class PageNumberReferenceSegment extends PageNumberReference implements Segment {
	
	public PageNumberReferenceSegment(String refid, NumeralStyle style) {
		super(refid, style);
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Reference;
	}

}