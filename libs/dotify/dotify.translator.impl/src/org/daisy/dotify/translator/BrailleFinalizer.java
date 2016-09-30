package org.daisy.dotify.translator;

public interface BrailleFinalizer {
	
	/**
	 * Finalizes braille translation, replacing remaining non braille characters
	 * with braille characters. An implementation can assume that the input has been
	 * filtered. The resulting string must have the same length as the input string.
	 * @param input the input string, mostly braille
	 * @return returns the finalized string
	 */
	public String finalizeBraille(String input);

}
