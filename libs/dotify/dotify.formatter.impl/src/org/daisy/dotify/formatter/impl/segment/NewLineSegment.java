package org.daisy.dotify.formatter.impl.segment;



public class NewLineSegment implements Segment {
	
	public NewLineSegment() {
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.NewLine;
	}

}