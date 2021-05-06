package org.daisy.dotify.translator;

/**
 * Provides a marker definition that can be used when applying markers to a
 * text.
 *
 * @author Joel Håkansson
 */
public class Marker {
    private final String prefix, postfix;

    /**
     * Creates a new marker with the specified arguments.
     *
     * @param prefix  the marker prefix
     * @param postfix the marker postfix
     */
    public Marker(String prefix, String postfix) {
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

}
