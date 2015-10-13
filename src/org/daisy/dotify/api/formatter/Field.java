package org.daisy.dotify.api.formatter;

/**
 * Provides a field for page headers/footers
 * 
 * @author Joel Håkansson
 */
public interface Field {

	/**
	 * The text style of the field, or null if no special style is used.
	 * @return the text style
	 */
	public String getTextStyle();
}
