package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.formatter.impl.search.BlockLineLocation;

/**
 * Provides details about a line.
 *
 * @author Joel Håkansson
 */
public final class LineProperties {
    /**
     * Provides default line properties. The default is to allow hyphenation and to reserve no width.
     */
    public static final LineProperties DEFAULT = new LineProperties.Builder().build();
    private final boolean suppressHyphenation;
    private final int reservedWidth;
    private final BlockLineLocation lineBlock;

    /**
     * Provides a builder of line properties.
     */
    public static final class Builder {
        private boolean suppressHyphenation = false;
        private int reservedWidth = 0;
        private BlockLineLocation lineBlock = null;

        /**
         * Creates a new empty builder.
         */
        public Builder() {
        }


        /**
         * Suppresses hyphenation on a line.
         *
         * @param value true if hyphenation should be suppressed, false otherwise.
         * @return this builder
         */
        public Builder suppressHyphenation(boolean value) {
            this.suppressHyphenation = value;
            return this;
        }

        /**
         * Reserves a number of characters on a line.
         *
         * @param value the number of characters to reserve.
         * @return this builder
         */
        public Builder reservedWidth(int value) {
            if (value < 0) {
                throw new IllegalArgumentException("Negative value not allowed: " + value);
            }
            this.reservedWidth = value;
            return this;
        }

        public Builder lineBlockLocation(BlockLineLocation value) {
            this.lineBlock = value;
            return this;
        }

        /**
         * Creates a new {@link LineProperties} with the current settings.
         *
         * @return a new line properties instance.
         */
        public LineProperties build() {
            return new LineProperties(this);
        }
    }

    private LineProperties(Builder builder) {
        this.suppressHyphenation = builder.suppressHyphenation;
        this.reservedWidth = builder.reservedWidth;
        this.lineBlock = builder.lineBlock;
    }

    /**
     * When true, hyphenation should be suppressed for the line.
     *
     * @return true if hyphenation should be suppressed, false otherwise
     */
    public boolean suppressHyphenation() {
        return suppressHyphenation;
    }

    /**
     * Gets the number of characters on a line that cannot be used for text flow contents.
     *
     * @return the number of characters on a line that cannot be used for text flow contents
     */
    public int getReservedWidth() {
        return reservedWidth;
    }

    public BlockLineLocation getBlockLineLocation() {
        return lineBlock;
    }
}
