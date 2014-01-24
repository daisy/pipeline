package org.daisy.dotify.api.formatter;

public interface FormatterCore {
	
	/**
	 * Start a new Sequence at the current position in the flow.
	 * @param props the SequenceProperties for the new sequence
	 */
	public void newSequence(SequenceProperties props);

	/**
	 * Start a new block with the supplied BlockProperties.
	 * @param props the BlockProperties of the new block
	 */
	public void startBlock(BlockProperties props);
	
	public void startBlock(BlockProperties props, String blockId);

	/**
	 * End the current block
	 */
	public void endBlock();
	
	/**
	 * Start a floating element
	 * @param id the identifier of the floating element
	 */
	public void startFloat(String id);
	
	/**
	 * End the floating element
	 */
	public void endFloat();

	/**
	 * Insert a marker at the current position in the flow
	 * @param marker the marker to insert
	 */
	public void insertMarker(Marker marker);
	
	/**
	 * Insert an anchor at the current position in the flow
	 * @param ref anchor name
	 */
	public void insertAnchor(String ref);
	
	/**
	 * Insert a leader at the current position in the flow
	 * @param leader the leader to insert
	 */
	public void insertLeader(Leader leader);
	
	/**
	 * Add chars to the current block
	 * @param chars the characters to add to the flow
	 * @param props the SpanProperties for the characters 
	 */
	public void addChars(CharSequence chars, TextProperties props);
	
	/**
	 * Explicitly break the current line, even if the line has space 
	 * left for more characters. The current block remains open.
	 */
	public void newLine();
	
	public void insertReference(String identifier, NumeralStyle numeralStyle);

}
