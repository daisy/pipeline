package org.daisy.dotify.api.formatter;

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
