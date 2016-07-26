package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.FormatterSequence;
import org.daisy.dotify.api.formatter.SequenceProperties;

/**
 * Provides an interface for a sequence of block contents.
 * 
 * @author Joel Håkansson
 */
class BlockSequence extends FormatterCoreImpl implements FormatterSequence {
	private static final long serialVersionUID = -6105005856680272131L;
	private final LayoutMaster master;
	private final SequenceProperties props;
	
	public BlockSequence(FormatterContext fc, SequenceProperties props, LayoutMaster master) {
		super(fc);
		this.props = props;
		this.master = master;
	}

	/**
	 * Gets the layout master for this sequence
	 * @return returns the layout master for this sequence
	 */
	public LayoutMaster getLayoutMaster() {
		return master;
	}

	/**
	 * Gets the block with the specified index, where index >= 0 && index < getBlockCount()
	 * @param index the block index
	 * @return returns the block index
	 * @throws IndexOutOfBoundsException if index < 0 || index >= getBlockCount()
	 */
	private Block getBlock(int index) {
		return this.elementAt(index);
	}

	/**
	 * Gets the number of blocks in this sequence
	 * @return returns the number of blocks in this sequence
	 */
	private int getBlockCount() {
		return this.size();
	}

	/**
	 * Get the initial page number, i.e. the number that the first page in the sequence should have
	 * @return returns the initial page number, or null if no initial page number has been specified
	 */
	public Integer getInitialPageNumber() {
		return props.getInitialPageNumber();
	}
	
	public SequenceProperties getSequenceProperties() {
		return props;
	}
	
	/**
	 * Gets the minimum number of rows that the specified block requires to begin 
	 * rendering on a page.
	 * 
	 * @param block the block to get the 
	 * @param refs
	 * @return the minimum number of rows
	 */
	public int getKeepHeight(Block block, BlockContext bc) {
		return getKeepHeight(this.indexOf(block), bc);
	}
	@SuppressWarnings("deprecation")
	private int getKeepHeight(int gi, BlockContext bc) {
		//FIXME: this assumes that row spacing is equal to 1
		//FIXME: what about borders?
		int keepHeight = getBlock(gi).getRowDataProperties().getOuterSpaceBefore()+getBlock(gi).getRowDataProperties().getInnerSpaceBefore()+getBlock(gi).getBlockContentManager(bc).getRowCount();
		if (getBlock(gi).getKeepWithNext()>0 && gi+1<getBlockCount()) {
			keepHeight += getBlock(gi).getRowDataProperties().getOuterSpaceAfter()+getBlock(gi).getRowDataProperties().getInnerSpaceAfter()
						+getBlock(gi+1).getRowDataProperties().getOuterSpaceBefore()+getBlock(gi+1).getRowDataProperties().getInnerSpaceBefore()+getBlock(gi).getKeepWithNext();
			switch (getBlock(gi+1).getKeepType()) {
				case ALL: case PAGE:
					keepHeight += getKeepHeight(gi+1, bc);
					break;
				case AUTO: break;
				default:;
			}
		}
		return keepHeight;
	}

}
