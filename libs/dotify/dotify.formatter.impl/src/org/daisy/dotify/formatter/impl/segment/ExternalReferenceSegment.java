package org.daisy.dotify.formatter.impl.segment;

/**
 * This segment contains all attributes from the optional
 * external-reference tags that could be added to blocks to
 * track and transmit information in the OBFL document that
 * is required for the PEF document.
 */
public class ExternalReferenceSegment implements Segment {
    private Object reference;

    public ExternalReferenceSegment(Object reference) {
        this.reference = reference;
    }

    public Object getExternalReference() {
        return reference;
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.ExternalReference;
    }

    @Override
    public String peek() {
        return "";
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public String resolve() {
        return "";
    }
}
