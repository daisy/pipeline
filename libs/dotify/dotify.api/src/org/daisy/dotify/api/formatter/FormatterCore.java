package org.daisy.dotify.api.formatter;

/**
 * Provides core formatter tasks.
 * 
 * @author Joel Håkansson
 */
public interface FormatterCore {

	/**
	 * Start a new block with the supplied BlockProperties.
	 * @param props the BlockProperties of the new block
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void startBlock(BlockProperties props);
	
	/**
	 * Start a new block with the supplied BlockProperties.
	 * @param props the block properties
	 * @param blockId the block id
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void startBlock(BlockProperties props, String blockId);

	/**
	 * End the current block.
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void endBlock();

	/**
	 * Insert a marker at the current position in the flow
	 * @param marker the marker to insert
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void insertMarker(Marker marker);
	
	/**
	 * Insert an anchor at the current position in the flow
	 * @param ref anchor name
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void insertAnchor(String ref);
	
	/**
	 * Insert a leader at the current position in the flow
	 * @param leader the leader to insert
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void insertLeader(Leader leader);
	
	/**
	 * Add chars to the current block
	 * @param chars the characters to add to the flow
	 * @param props the SpanProperties for the characters 
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void addChars(CharSequence chars, TextProperties props);
	
	/**
	 * Starts a style section
	 * @param style the name of the style
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void startStyle(String style);
	
	/**
	 * Ends a previously opened style section
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void endStyle();
	
	/**
	 * Explicitly break the current line, even if the line has space 
	 * left for more characters. The current block remains open.
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void newLine();
	
	/**
	 * Adds the page number of a reference.
	 * 
	 * @param identifier the element of interest
	 * @param numeralStyle the numeral style
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void insertReference(String identifier, NumeralStyle numeralStyle);
	
	/**
	 * Inserts an expression to evaluate.
	 * @param exp the expression
	 * @param t the text properties
	 */
	public void insertEvaluate(DynamicContent exp, TextProperties t);
	
	/**
	 * Inserts a dynamic layout processor
	 * @param renderer the layout processor
	 */
	public void insertDynamicLayout(DynamicRenderer renderer);
	
	/**
	 * Starts a new table with the supplied properties.
	 * @param props the table properties
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void startTable(TableProperties props);
	
	/**
	 * <p>Marks the beginning of the table header part of a table. The table header is used
	 * for repeating cells after page breaks within a table. This call can only be made
	 * directly after start table.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table header is implied in the call to
	 * <code>beginsTableBody</code>.</p>
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void beginsTableHeader();
	
	/**
	 * <p>Starts the table body. This call can only be made once per table.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table body is implied in the call to
	 * <code>endTable</code>.</p>
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void beginsTableBody();
	
	/**
	 * <p>Starts a table row.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table row is implied in the call to
	 * <code>beginsTableRow</code> or <code>endTable</code>.</p>
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void beginsTableRow();
	
	/**
	 * <p>Starts a table cell.</p>
	 * <p>Note that this is not a hierarchical call that is matched
	 * by a corresponding end method. The end of the table cell is implied in the call to
	 * <code>beginsTableCell</code>, <code>beginsTableRow</code> or <code>endTable</code>.</p>
	 * @param props the cell properties
	 * @return returns a formatter core for the cell
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public FormatterCore beginsTableCell(TableCellProperties props);

	/**
	 * Ends the table.
	 * @throws IllegalStateException if the current state does not allow this call to be made
	 */
	public void endTable();

}
