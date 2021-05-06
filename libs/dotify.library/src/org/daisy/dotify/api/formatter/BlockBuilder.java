package org.daisy.dotify.api.formatter;

/**
 * Provides methods that supply content in a hierarchy of blocks.
 *
 * @author Joel Håkansson
 */
public interface BlockBuilder extends BlockContentBuilder {

    /**
     * Start a new block with the supplied BlockProperties.
     *
     * @param props the BlockProperties of the new block
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void startBlock(BlockProperties props);

    /**
     * Start a new block with the supplied BlockProperties.
     *
     * @param props   the block properties
     * @param blockId the block id
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void startBlock(BlockProperties props, String blockId);

    /**
     * End the current block.
     *
     * @throws IllegalStateException if the current state does not allow this call to be made
     */
    public void endBlock();

}
