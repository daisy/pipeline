package org.daisy.dotify.formatter.impl.segment;

/**
 * TODO: Write java doc.
 */
public class NewLineSegment implements Segment {

    public NewLineSegment() {
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.NewLine;
    }

    @Override
    public String peek() {
        return "\n";
    }

    @Override
    public String resolve() {
        return "\n";
    }

    @Override
    public boolean isStatic() {
        return true;
    }

}
