package org.daisy.dotify.api.formatter;


/**
 * Provides an interface for a sequence of block contents.
 * 
 * @author Joel Håkansson
 */
public interface BlockSequence extends Iterable<Block> {
	/**
	 * Get the initial page number, i.e. the number that the first page in the sequence should have
	 * @return returns the initial page number, or null if no initial page number has been specified
	 */
	public Integer getInitialPageNumber();
	/*
	 * Gets the number of blocks in this sequence
	 * @return returns the number of blocks in this sequence
	 */
	//public int getBlockCount();
	/*
	 * Gets the block with the specified index, where index >= 0 && index < getBlockCount()
	 * @param index the block index
	 * @return returns the block index
	 * @throws IndexOutOfBoundsException if index < 0 || index >= getBlockCount()
	 *
	public Block getBlock(int index);*/
	
	/**
	 * Gets the minimum number of rows that the specified block requires to begin 
	 * rendering on a page.
	 * 
	 * @param block the block to get the 
	 * @param refs
	 * @return the minimum number of rows
	 */
	public int getKeepHeight(Block block, CrossReferences refs);
	
	/**
	 * Gets the layout master for this sequence
	 * @return returns the layout master for this sequence
	 */
	public LayoutMaster getLayoutMaster();

}