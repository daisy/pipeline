package org.daisy.dotify.formatter.impl;



class NewLineSegment implements Segment {
	
	public NewLineSegment() {
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.NewLine;
	}

}