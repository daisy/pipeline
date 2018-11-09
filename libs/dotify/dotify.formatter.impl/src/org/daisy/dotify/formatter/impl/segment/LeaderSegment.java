package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.Leader;

public class LeaderSegment extends Leader implements Segment{
	
	protected LeaderSegment(Builder builder) {
		super(builder);
	}
	
	public LeaderSegment(Leader leader) {
		super(leader);
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Leader;
	}

}
