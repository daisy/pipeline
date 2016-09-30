package org.daisy.dotify.common.text;

/**
 * Data object returned by StringSplitter containing a sub sequence along with
 * match status for the sub sequence.
 * 
 * @author Joel Håkansson
 */
public class SplitResult {
	private final String text;
	private final boolean match;
	
	/**
	 * Create a new SplitResult
	 * @param text the result text
	 * @param match set to true if the text matched the regular expression used to extract the text, false otherwise
	 */
	SplitResult(String text, boolean match) {
		this.text = text;
		this.match = match;
	}

	/**
	 * Gets the text in this result
	 * @return returns the text in this result
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns true if this sub sequence matched the regular expression used when creating this SplitResult 
	 * @return returns true if this sub sequence matched the regular expression, false otherwise
	 */
	public boolean isMatch() {
		return match;
	}

}
