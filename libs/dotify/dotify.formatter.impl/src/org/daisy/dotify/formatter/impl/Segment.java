package org.daisy.dotify.formatter.impl;

interface Segment {
	//{PCDATA, LEADER, MARKER, ANCHOR, BR, EVALUATE, BLOCK, TOC_ENTRY, PAGE_NUMBER}
	enum SegmentType {Text, NewLine, Leader, Reference, Marker, Anchor, Evaluate};
	
	public SegmentType getSegmentType();

}