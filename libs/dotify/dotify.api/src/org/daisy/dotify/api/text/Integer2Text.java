package org.daisy.dotify.api.text;


/**
 * Provides an integer2text.
 * 
 * @author Joel Håkansson
 */
public interface Integer2Text {
	
	/**
	 * Converts the integer to text.
	 * 
	 * @param value
	 *            the integer value
	 * @throws IntegerOutOfRange
	 *             If value is out of range of the implementations
	 *             capabilities.
	 * @return the integer as text
	 */
	public String intToText(int value) throws IntegerOutOfRange;
	
}
