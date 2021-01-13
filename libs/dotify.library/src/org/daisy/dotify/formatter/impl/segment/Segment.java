package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.translator.FollowingText;
import org.daisy.dotify.api.translator.ResolvableText;

import java.util.Optional;

/**
 * TODO: Write java doc.
 */
public interface Segment extends ResolvableText {
    /**
     * {PCDATA, LEADER, MARKER, ANCHOR, BR, EVALUATE, BLOCK, TOC_ENTRY, PAGE_NUMBER}.
     */
    enum SegmentType {
        Text, NewLine, Leader, PageReference, Marker, Anchor, Identifier, Evaluate, Style,
        MarkerReference, ExternalReference
    };

    public SegmentType getSegmentType();

    @Override
    public default Optional<String> getLocale() {
        return Optional.empty();
    }

    @Override
    public default boolean shouldHyphenate() {
        return false;
    }

    @Override
    public default boolean shouldMarkCapitalLetters() {
        return true;
    }

    /**
     * Return a version of the segment for use as context when the segment ought to be kept on a
     * single row, possibly together with other segments. This is the case for content following a
     * right-aligned leader.
     *
     * @return The "unbreakable" version of the segment.
     */
    public default FollowingText asUnbreakable() {
        String value = peek();
        String unbreakableValue = value.replaceAll("[\\s\u2800]", "\u00A0");
        if (unbreakableValue.equals(value)) {
            return this;
        } else {
            return new FollowingText() {
                @Override
                public String peek() {
                    return unbreakableValue;
                }
                @Override
                public boolean isStatic() {
                    return Segment.this.isStatic();
                }
                @Override
                public Optional<String> getLocale() {
                    return Segment.this.getLocale();
                }
                @Override
                public boolean shouldHyphenate() {
                    return Segment.this.shouldHyphenate();
                }
                @Override
                public boolean shouldMarkCapitalLetters() {
                    return Segment.this.shouldMarkCapitalLetters();
                }
            };
        }
    }
}
