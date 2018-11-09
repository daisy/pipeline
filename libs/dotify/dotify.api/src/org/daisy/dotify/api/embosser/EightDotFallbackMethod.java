package org.daisy.dotify.api.embosser;
/**
 * Defines the fallback action when a character in the range 0x2840-0x28FF is
 * encountered.
 */
public enum EightDotFallbackMethod {
	/**
	 * Mask the character. Treat it as if dots 7 and 8 were off
	 */
	MASK,
	/**
	 * Replace the character with a fixed replacement character
	 */
	REPLACE,
	/**
	 * Remove the character from output
	 */
	REMOVE
}; // , FAIL