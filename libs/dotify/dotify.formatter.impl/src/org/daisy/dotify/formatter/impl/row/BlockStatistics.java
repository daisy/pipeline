package org.daisy.dotify.formatter.impl.row;

public interface BlockStatistics {
	
	/**
	 * Gets the number of forced line breaks.
	 * @return the number of forced line breaks
	 * @throws IllegalStateException if the force break count cannot be returned 
	 */
	int getForceBreakCount();
	
	/**
	 * Gets the minimum width available for content (excluding margins)
	 * @return returns the available width, in characters
	 */
	int getMinimumAvailableWidth();
	
	/**
	 * Gets the number of rows produced.
	 * @return the number of rows produced
	 * @throws IllegalStateException if the row count cannot be returned
	 */
	int getRowCount();

}
