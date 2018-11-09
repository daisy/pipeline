package org.daisy.dotify.formatter.impl.core;

import java.util.List;

import org.daisy.dotify.formatter.impl.row.RowImpl;

/**
 * Provides a cost function for table layout
 * @author Joel Håkansson
 *
 */
public interface TableCost {

	/**
	 * Gets the cost of the layout.
	 * @return returns the cost
	 */
	public double getCost();
	
	/**
	 * Adds a cell to the cost calculation.
	 * @param rows the rows in the cell
	 * @param cellWidth the cell width
	 * @param 	forceCount the number of forced line breaks. In other words, breaks 
	 * 			not preferred by the line breaking algorithm
	 */
	public void addCell(List<RowImpl> rows, int cellWidth, int forceCount);
	
	/**
	 * Adds the complete table to the cost calculation. Note that this will include
	 * the content previously added with {@link #addCell(List, int, int)}.
	 * @param rows the completed rows
	 * @param columnCount the number of columns
	 */
	public void completeTable(List<RowImpl> rows, int columnCount);
}
