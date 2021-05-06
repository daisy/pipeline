package org.daisy.braille.utils.impl.tools.embosser;

import org.daisy.dotify.api.embosser.EmbosserWriterProperties;

/**
 * Extends {@link EmbosserWriterProperties} with support for duplex, which is
 * needed by the implementations.
 *
 * @author Joel Håkansson
 */
public interface InternalEmbosserWriterProperties extends EmbosserWriterProperties {
    /**
     * Returns true if this embosser supports duplex printing.
     *
     * @return returns true if this embosser supports duplex printing
     */
    public boolean supportsDuplex();

    /**
     * Gets the maximum page height in the current configuration.
     *
     * @return returns the maximum page height, in rows
     */
    public int getMaxRowCount();
}
