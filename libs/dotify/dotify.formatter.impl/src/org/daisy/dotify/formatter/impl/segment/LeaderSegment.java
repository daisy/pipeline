package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.Leader;

/**
 * TODO: Write java doc.
 */
public class LeaderSegment extends Leader implements Segment {

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

    @Override
    public String peek() {
        return getPattern();
    }

    @Override
    public String resolve() {
        return getPattern();
    }

    @Override
    public boolean isStatic() {
        return true;
    }

}
