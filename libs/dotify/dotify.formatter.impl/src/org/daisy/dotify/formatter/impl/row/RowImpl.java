package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.api.formatter.FormattingTypes.Alignment;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.writer.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a single row of text. {@link RowImpl}s are immutable.
 *
 * @author Joel Håkansson
 */
public final class RowImpl implements Row {
    private final String chars;
    private final List<Marker> markers;
    private final List<String> anchors;
    private final List<String> identifiers;
    private final MarginProperties leftMargin;
    private final MarginProperties rightMargin;
    private final Alignment alignment;
    private final Float rowSpacing;
    private final boolean adjustedForMargin;
    private final boolean allowsBreakAfter;
    private final int leaderSpace;
    private final Object externalReference;
    private final boolean invisible;

    /**
     * TODO: Write java doc.
     */
    public static class Builder {
        private String chars;
        private List<Marker> markers = new ArrayList<>();
        private List<String> anchors = new ArrayList<>();
        private List<String> identifiers = new ArrayList<>();
        private MarginProperties leftMargin = MarginProperties.EMPTY_MARGIN;
        private MarginProperties rightMargin = MarginProperties.EMPTY_MARGIN;
        private Alignment alignment = Alignment.LEFT;
        private Float rowSpacing = null;
        private boolean adjustedForMargin = false;
        private boolean allowsBreakAfter = true;
        private int leaderSpace = 0;
        private boolean built = false;
        private Object externalReference;
        private boolean invisible;

        public Builder(String chars) {
            this.chars = chars;
        }

        public Builder(RowImpl template) {
            this.chars = template.chars;
            this.markers = new ArrayList<>(template.markers);
            this.anchors = new ArrayList<>(template.anchors);
            this.identifiers = new ArrayList<>(template.identifiers);
            this.leftMargin = template.leftMargin;
            this.rightMargin = template.rightMargin;
            this.alignment = template.alignment;
            this.rowSpacing = template.rowSpacing;
            this.adjustedForMargin = template.adjustedForMargin;
            this.allowsBreakAfter = template.allowsBreakAfter;
            this.leaderSpace = template.leaderSpace;
            this.externalReference = template.externalReference;
            this.invisible = template.invisible;
        }

        Builder(RowImpl.Builder template) {
            this.chars = template.chars;
            this.markers = new ArrayList<>(template.markers);
            this.anchors = new ArrayList<>(template.anchors);
            this.identifiers = new ArrayList<>(template.identifiers);
            this.leftMargin = template.leftMargin;
            this.rightMargin = template.rightMargin;
            this.alignment = template.alignment;
            this.rowSpacing = template.rowSpacing;
            this.adjustedForMargin = template.adjustedForMargin;
            this.allowsBreakAfter = template.allowsBreakAfter;
            this.leaderSpace = template.leaderSpace;
            this.externalReference = template.externalReference;
            this.invisible = template.invisible;
        }

        public Builder text(String value) {
            this.chars = value;
            return this;
        }

        //TODO: this isn't according to the builder pattern, but we'll allow it as a transition
        String getText() {
            return chars;
        }

        public Builder leftMargin(MarginProperties value) {
            this.leftMargin = value;
            return this;
        }

        //TODO: this isn't according to the builder pattern, but we'll allow it as a transition
        MarginProperties getLeftMargin() {
            return leftMargin;
        }

        public Builder rightMargin(MarginProperties value) {
            this.rightMargin = value;
            return this;
        }

        public Builder alignment(Alignment value) {
            this.alignment = value;
            return this;
        }

        public Builder rowSpacing(Float value) {
            this.rowSpacing = value;
            return this;
        }

        public Builder adjustedForMargin(boolean value) {
            this.adjustedForMargin = value;
            return this;
        }

        public Builder allowsBreakAfter(boolean value) {
            this.allowsBreakAfter = value;
            return this;
        }

        public Builder addAnchors(List<String> refs) {
            assertNotBuilt();
            anchors.addAll(refs);
            return this;
        }

        /**
         * Adds an anchor to the Row.
         *
         * @param ref the anchor
         * @return returns this builder
         */
        Builder addAnchor(String ref) {
            assertNotBuilt();
            anchors.add(ref);
            return this;
        }

        public void addAnchors(int index, List<String> refs) {
            assertNotBuilt();
            anchors.addAll(index, refs);
        }

        public void leaderSpace(int value) {
            this.leaderSpace = value;
        }

        //TODO: this isn't according to the builder pattern, but we'll allow it as a transition
        public int getLeaderSpace() {
            return leaderSpace;
        }

        /**
         * Add a collection of markers to the Row.
         *
         * @param list the list of markers
         * @return returns this builder
         */
        public Builder addMarkers(List<Marker> list) {
            assertNotBuilt();
            markers.addAll(list);
            return this;
        }

        /**
         * Add a marker to the Row.
         *
         * @param value the marker
         * @return returns this builder
         */
        Builder addMarker(Marker value) {
            assertNotBuilt();
            markers.add(value);
            return this;
        }

        /**
         * Add a collection of markers to the Row.
         *
         * @param index the position in the marker list to insert the markers
         * @param list  the list of markers
         * @throws IndexOutOfBoundsException if the index is out of range
         *                                   (<code>index &lt; 0 || index &gt; getMarkers().size()</code>)
         */
        public void addMarkers(int index, List<Marker> list) {
            assertNotBuilt();
            markers.addAll(index, list);
        }

        /**
         * Add a collection of identifiers to the Row.
         *
         * @param refs a list of identifiers
         * @return returns this builder
         */
        public Builder addIdentifiers(List<String> refs) {
            assertNotBuilt();
            identifiers.addAll(refs);
            return this;
        }

        /**
         * Add an external reference object that will flow though the framework to the PEF file.
         *
         * @param externalReference External reference object
         * @return returns this builder
         */
        public Builder addExternalReference(Object externalReference) {
            this.externalReference = externalReference;
            return this;
        }

        /**
         * Add an identifier to the Row.
         *
         * @param id the identifier
         * @return returns this builder
         */
        Builder addIdentifier(String id) {
            assertNotBuilt();
            identifiers.add(id);
            return this;
        }

        /**
         * Add a collection of identifiers to the Row.
         *
         * @param index the position in the identifier list to insert the identifiers
         * @param list  the list of identifiers
         * @throws IndexOutOfBoundsException if the index is out of range
         *                                   (<code>index &lt; 0 || index &gt; getIdentifiers().size()</code>)
         */
        public void addIdentifiers(int index, List<String> list) {
            assertNotBuilt();
            identifiers.addAll(index, list);
        }

        private void assertNotBuilt() {
            // We're using this method to check if the builder has been used instead of
            // copying the internal lists. This is assumed to be faster.
            if (built) {
                throw new IllegalStateException("Cannot build more than once.");
            }
        }

        public Builder invisible(boolean b) {
            this.invisible = b;
            return this;
        }

        public RowImpl build() {
            assertNotBuilt();
            built = true;
            return new RowImpl(this);
        }
    }

    private RowImpl(Builder builder) {
        this.chars = builder.chars;
        this.markers = Collections.unmodifiableList(builder.markers);
        this.anchors = Collections.unmodifiableList(builder.anchors);
        this.identifiers = Collections.unmodifiableList(builder.identifiers);
        this.leftMargin = builder.leftMargin;
        this.rightMargin = builder.rightMargin;
        this.alignment = builder.alignment;
        this.rowSpacing = builder.rowSpacing;
        this.adjustedForMargin = builder.adjustedForMargin;
        this.allowsBreakAfter = builder.allowsBreakAfter;
        this.leaderSpace = builder.leaderSpace;
        this.externalReference = builder.externalReference;
        this.invisible = builder.invisible;
    }

    /**
     * Create a new Row.
     *
     * @param chars the characters on this row
     */
    public RowImpl(String chars) {
        this(chars, new MarginProperties(), new MarginProperties());
    }

    public RowImpl(String chars, MarginProperties leftMargin, MarginProperties rightMargin) {
        this.chars = chars;
        this.markers = Collections.emptyList();
        this.anchors = Collections.emptyList();
        this.identifiers = Collections.emptyList();
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.alignment = Alignment.LEFT;
        this.rowSpacing = null;
        this.adjustedForMargin = false;
        this.allowsBreakAfter = true;
        this.leaderSpace = 0;
        this.externalReference = null;
        this.invisible = false;
    }

    /**
     * Create a new empty Row.
     */
    public RowImpl() {
        this("");
    }

    /**
     * Get the characters on this row.
     *
     * @return returns the characters on the row
     */
    @Override
    public String getChars() {
        return chars;
    }

    public int getLeaderSpace() {
        return leaderSpace;
    }

    public int getWidth() {
        return chars.length() + leftMargin.getContent().length() + rightMargin.getContent().length();
    }

    /**
     * Get all markers on this Row.
     *
     * @return returns the markers
     */
    public List<Marker> getMarkers() {
        return markers;
    }

    public boolean hasMarkerWithName(String name) {
        for (Marker m : markers) {
            if (m.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all anchors on this Row.
     *
     * @return returns an ArrayList of anchors
     */
    public List<String> getAnchors() {
        return anchors;
    }

    /**
     * Get all identifiers on this Row.
     *
     * @return returns an ArrayList of identifiers
     */
    public List<String> getIdentifiers() {
        return identifiers;
    }

    /**
     * Get the left margin value for the Row, in characters.
     *
     * @return returns the left margin
     */
    public MarginProperties getLeftMargin() {
        return leftMargin;
    }

    public MarginProperties getRightMargin() {
        return rightMargin;
    }

    /**
     * Gets the alignment value for the row.
     *
     * @return returns the alignment
     */
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public Float getRowSpacing() {
        return rowSpacing;
    }

    public boolean shouldAdjustForMargin() {
        return adjustedForMargin;
    }

    public boolean allowsBreakAfter() {
        return allowsBreakAfter;
    }

    public boolean isInvisible() {
        return invisible;
    }

    /**
     * Get the current external reference information tagged for this row.
     *
     * @return  Object of the external reference.
     */
    public Object getExternalReference() {
        return externalReference;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (adjustedForMargin ? 1231 : 1237);
        result = prime * result + ((alignment == null) ? 0 : alignment.hashCode());
        result = prime * result + (allowsBreakAfter ? 1231 : 1237);
        result = prime * result + ((anchors == null) ? 0 : anchors.hashCode());
        result = prime * result + ((chars == null) ? 0 : chars.hashCode());
        result = prime * result + leaderSpace;
        result = prime * result + ((leftMargin == null) ? 0 : leftMargin.hashCode());
        result = prime * result + ((markers == null) ? 0 : markers.hashCode());
        result = prime * result + ((rightMargin == null) ? 0 : rightMargin.hashCode());
        result = prime * result + ((rowSpacing == null) ? 0 : rowSpacing.hashCode());
        result = prime * result + ((identifiers == null) ? 0 : identifiers.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RowImpl other = (RowImpl) obj;
        if (adjustedForMargin != other.adjustedForMargin) {
            return false;
        }
        if (alignment != other.alignment) {
            return false;
        }
        if (allowsBreakAfter != other.allowsBreakAfter) {
            return false;
        }
        if (anchors == null) {
            if (other.anchors != null) {
                return false;
            }
        } else if (!anchors.equals(other.anchors)) {
            return false;
        }
        if (chars == null) {
            if (other.chars != null) {
                return false;
            }
        } else if (!chars.equals(other.chars)) {
            return false;
        }
        if (leaderSpace != other.leaderSpace) {
            return false;
        }
        if (leftMargin == null) {
            if (other.leftMargin != null) {
                return false;
            }
        } else if (!leftMargin.equals(other.leftMargin)) {
            return false;
        }
        if (markers == null) {
            if (other.markers != null) {
                return false;
            }
        } else if (!markers.equals(other.markers)) {
            return false;
        }
        if (rightMargin == null) {
            if (other.rightMargin != null) {
                return false;
            }
        } else if (!rightMargin.equals(other.rightMargin)) {
            return false;
        }
        if (rowSpacing == null) {
            if (other.rowSpacing != null) {
                return false;
            }
        } else if (!rowSpacing.equals(other.rowSpacing)) {
            return false;
        }
        if (identifiers == null) {
            if (other.identifiers != null) {
                return false;
            }
        } else if (!identifiers.equals(other.identifiers)) {
            return false;
        }
        return true;
    }

}
