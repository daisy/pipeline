package org.daisy.dotify.api.writer;

/**
 * Defines a row of braille.
 * 
 * @author Joel Håkansson
 */
public interface Row {

	/**
	 * Gets the characters 
	 * @return returns the characters
	 */
	public String getChars();

	/**
	 * Gets the row spacing, in rows
	 * @return the row spacing, or null if not set
	 */
	public Float getRowSpacing();
}
