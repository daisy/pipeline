package org.daisy.dotify.formatter.impl.search;

/**
 * Provides the overhead associated with a volume of braille.
 *
 * Objects are immutable.
 *
 * @author Joel Håkansson
 */
public final class Overhead {
    private final int preContentSize;
    private final int postContentSize;

    /**
     * @param preContentSize  the pre-content size, in sheets
     * @param postContentSize the post-content size, in sheets
     */
    Overhead(int preContentSize, int postContentSize) {
        super();
        this.preContentSize = preContentSize;
        this.postContentSize = postContentSize;
    }

    /**
     * Creates a new instance with the specified pre-content size.
     * The post-content size is unchanged.
     *
     * @param s the pre-content size, in sheets
     * @return a new instance
     */
    public Overhead withPreContentSize(int s) {
        return new Overhead(s, getPostContentSize());
    }

    /**
     * Creates a new instance with the specified post-content size.
     * The pre-content size is unchanged.
     *
     * @param s the post-content size, in sheets
     * @return a new instance
     */
    public Overhead withPostContentSize(int s) {
        return new Overhead(getPreContentSize(), s);
    }

    /**
     * Gets the pre-content size, in sheets.
     *
     * @return the pre-content size
     */
    public int getPreContentSize() {
        return preContentSize;
    }

    /**
     * Gets the post-content size, in sheets.
     *
     * @return the post-content size
     */
    public int getPostContentSize() {
        return postContentSize;
    }

    /**
     * Gets the total overhead, in other words the
     * pre-content size plus the post-content size.
     *
     * @return the total overhead, in sheets
     */
    public int total() {
        return preContentSize + postContentSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + preContentSize;
        result = prime * result + postContentSize;
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
        Overhead other = (Overhead) obj;
        if (preContentSize != other.preContentSize) {
            return false;
        }
        if (postContentSize != other.postContentSize) {
            return false;
        }
        return true;
    }
}
