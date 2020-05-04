package org.daisy.dotify.formatter.impl.segment;

/**
 * TODO: Write java doc.
 */
public final class MarkerValue {
    private final String prefix, postfix;

    /**
     * Creates a new marker with the specified arguments.
     *
     * @param prefix  the marker prefix
     * @param postfix the marker postfix
     */
    public MarkerValue(String prefix, String postfix) {
        this.prefix = prefix;
        this.postfix = postfix;
    }

    /**
     * Gets the marker prefix.
     *
     * @return returns the marker prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the marker postfix.
     *
     * @return returns the marker postfix
     */
    public String getPostfix() {
        return postfix;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [prefix=" + prefix + ", postfix=" + postfix + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((postfix == null) ? 0 : postfix.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
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
        MarkerValue other = (MarkerValue) obj;
        if (postfix == null) {
            if (other.postfix != null) {
                return false;
            }
        } else if (!postfix.equals(other.postfix)) {
            return false;
        }
        if (prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!prefix.equals(other.prefix)) {
            return false;
        }
        return true;
    }

}
