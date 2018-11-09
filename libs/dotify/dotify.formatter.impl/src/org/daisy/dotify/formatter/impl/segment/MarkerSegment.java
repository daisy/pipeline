package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.Marker;

public class MarkerSegment extends Marker implements Segment {
	
	public MarkerSegment(Marker m) {
		super(m.getName(), m.getValue());
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Marker;
	}

}
