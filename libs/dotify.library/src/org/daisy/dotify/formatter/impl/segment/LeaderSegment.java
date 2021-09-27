package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.Leader;
import org.daisy.dotify.api.formatter.TextProperties;

/**
 * TODO: Write java doc.
 */
public class LeaderSegment extends Leader implements Segment {

    private final TextProperties tp;

    public LeaderSegment(Leader leader, TextProperties tp) {
        super(leader);
        this.tp = tp;
    }

    public TextProperties getTextProperties() {
        return tp;
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
