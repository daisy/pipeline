package org.daisy.dotify.formatter.impl.segment;

/**
 * TODO: Write java doc.
 */
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
