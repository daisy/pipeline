package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.Marker;

/**
 * TODO: Write java doc.
 */
public class MarkerSegment extends Marker implements Segment {

    public MarkerSegment(Marker m) {
        super(m.getName(), m.getValue());
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.Marker;
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
