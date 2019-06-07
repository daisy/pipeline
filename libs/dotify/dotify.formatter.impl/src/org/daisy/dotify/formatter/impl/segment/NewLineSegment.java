package org.daisy.dotify.formatter.impl.segment;

public class NewLineSegment implements Segment {
	
	public NewLineSegment() {
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.NewLine;
	}

	@Override
	public String peek() {
		return "";
	}

	@Override
	public String resolve() {
		return "";
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}

}