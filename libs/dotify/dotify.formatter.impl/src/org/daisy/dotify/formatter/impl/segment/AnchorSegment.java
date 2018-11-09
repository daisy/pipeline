package org.daisy.dotify.formatter.impl.segment;


public class AnchorSegment implements Segment {
	private final String referenceID;
	
	public AnchorSegment(String referenceID) {
		this.referenceID = referenceID;
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Anchor;
	}

	public String getReferenceID() {
		return referenceID;
	}

}
