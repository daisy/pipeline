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

}
