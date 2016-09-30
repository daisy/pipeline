package org.daisy.dotify.common.text;

/**
 * StringFilter is in interface used when replacing a string with another string.
 * 
 * @author  Joel Hakansson
 */
public interface StringFilter {
	
	/**
	 * Replaces a string with another string based on rules determined by the
	 * implementing class.
	 * @param str the string to replace
	 * @return returns the replacement string
	 */
	public String filter(String str);

}
