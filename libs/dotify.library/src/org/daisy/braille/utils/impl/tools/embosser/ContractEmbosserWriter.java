package org.daisy.braille.utils.impl.tools.embosser;

import org.daisy.dotify.api.embosser.EmbosserWriter;

import java.io.IOException;

/**
 * Extends embosser writer with an internal contract.
 *
 * @author Joel Håkansson
 */
public interface ContractEmbosserWriter extends EmbosserWriter {
    /**
     * Opens for writing using the specified contract.
     *
     * @param duplex   if both sides of sheets should be used, false otherwise
     * @param contract the contract
     * @throws IOException                           if an I/O exception of some sort has occurred
     * @throws InternalContractNotSupportedException if the supplied contract is not supported, that is to say
     *                                               if the contract does not contain information required by
     *                                               the implementation
     */
    public void open(
        boolean duplex,
        InternalContract contract
    ) throws IOException, InternalContractNotSupportedException;
}
