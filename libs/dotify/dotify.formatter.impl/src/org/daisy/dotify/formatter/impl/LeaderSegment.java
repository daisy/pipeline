package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.Leader;

class LeaderSegment extends Leader implements Segment{
	
	protected LeaderSegment(Builder builder) {
		super(builder);
	}
	
	LeaderSegment(Leader leader) {
		super(leader);
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Leader;
	}

}
