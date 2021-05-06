package org.daisy.dotify.api.embosser;

/**
 * Provides information about the embosser.
 *
 * @author Joel Håkansson
 */
public interface EmbosserWriterProperties {

    /**
     * Gets the maximum row width in the current configuration.
     *
     * @return returns the maximum row width, in characters
     */
    public int getMaxWidth();

    /**
     * Returns true if this embosser supports aligning. This indicates
     * that rows can be padded with whitespace to move the text block
     * horizontally using the value returned by <code>getMaxWidth</code>.
     * Should return true for all physical embossers, since they all have
     * a finite row length.
     *
     * @return returns true if this embosser supports aligning, false otherwise.
     */
    public boolean supportsAligning();

}
