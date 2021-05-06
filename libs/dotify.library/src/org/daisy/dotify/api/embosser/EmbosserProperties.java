package org.daisy.dotify.api.embosser;


/**
 * Provides an interface for common embosser related properties.
 *
 * @author Joel Håkansson
 */
public interface EmbosserProperties {
    /**
     * Regular printing or multi-page printing.
     */
    public enum PrintMode {
        /**
         * One print page per input page.
         */
        REGULAR,
        /**
         * Two print pages per input page.
         */
        MAGAZINE
    }

    /**
     * Returns true if this embosser has some method for volume handling.
     *
     * @return returns true if this embosser supports volumes
     */
    public boolean supportsVolumes();

    /**
     * Returns true if this embosser supports 8 dot braille.
     *
     * @return returns true if this embosser supports 8 dot braille
     */
    public boolean supports8dot();

    /**
     * Returns true if this embosser supports duplex printing.
     *
     * @return returns true if this embosser supports duplex printing
     */
    public boolean supportsDuplex();

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

    /**
     * Returns true if this embosser supports z-folding. This indicates
     * that, if tractor paper is used, the embosser can emboss every
     * other paper upside down with the rear side up so that pages are
     * ordered face up as they fold naturally in the output stack.
     *
     * @return returns true if this embosser supports z-folding, false otherwise.
     */
    public boolean supportsZFolding();

    /**
     * Returns true if this embosser supports magazine layout. This indicates
     * that the embosser can reorder pages and emboss two pages side-by-side
     * on the same side of the paper (and two more on the other side), so that
     * a readable document is created by stapling and folding the output stack
     * in the middle.
     *
     * @param mode the print mode
     * @return returns true if this embosser supports magazine layout, false otherwise.
     */
    public boolean supportsPrintMode(PrintMode mode);

}
