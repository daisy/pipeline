package org.daisy.dotify.formatter.impl.segment;

public class IdentifierSegment implements Segment {
	
	private final String name;
	
	public IdentifierSegment(String name) {
		this.name = name;
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Identifier;
	}

	public String getName() {
		return name;
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
