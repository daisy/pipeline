package org.daisy.dotify.formatter.impl.row;

/**
 * Provides read/write access to block processing information and state.
 * 
 * @author Joel Håkansson
 */
interface SegmentProcessing {

	/**
	 * Returns true if the block hasn't finalized (flushed) any rows yet.
	 * @return true if no rows have been finalized, false otherwise.
	 */
	boolean isEmpty();

	/**
	 * Returns true if there is an active (not completed) row in the
	 * processor.
	 * @return true if there is an active row
	 */
	boolean hasCurrentRow();
	
	/**
	 * Flushes the active row and returns it. 
	 * @return returns the completed row
	 */
	RowImpl flushCurrentRow();
	
	/**
	 * Starts a new active row. Note that if {@link #hasCurrentRow()} returns
	 * true, {@link #flushCurrentRow()} should be called first.
	 * @param left the left margin
	 * @param right the right margin
	 */
	void newCurrentRow(MarginProperties left, MarginProperties right);
	
	/**
	 * Gets the active row.
	 * @return returns the active row
	 */
	RowImpl.Builder getCurrentRow();
	
	/**
	 * Gets the number of unused columns to the left of the text block. 
	 * Both margins and borders counts as unused columns in this case. 
	 * For left adjusted text, this value is typically equal to
	 * the left margin.
	 * @return returns the number of unused columns
	 */
	int getUnusedLeft();
	
	/**
	 * Gets the number of unused columns to the right of the text block.
	 * Both margins and borders counts as unused columns in this case.
	 * @return returns the number of unused columns
	 */
	int getUnusedRight();
	
	/**
	 * Adds to the running force count value.
	 * @param value the value to add
	 */
	void addToForceCount(double value);
	
	/**
	 * Gets the leader manager for this block processor.
	 * @return returns the leader manager
	 */
	LeaderManager getLeaderManager();
	
	/**
	 * Returns true if the block processor has a pending list item.
	 * @return true if the block processor has a pending list item, false otherwise
	 */
	boolean hasListItem();
	
	/**
	 * Gets the pending list item, or null if {@link #hasListItem()} returns false.
	 * @return returns the pending list item, or null
	 */
	ListItem getListItem();
	
	/**
	 * Discards the pending list item.
	 */
	void discardListItem();
}
