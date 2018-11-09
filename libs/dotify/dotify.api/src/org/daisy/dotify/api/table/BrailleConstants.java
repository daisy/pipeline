package org.daisy.dotify.api.table;

/**
 * Provides constants for braille.
 * @author Joel Håkansson
 */
public final class BrailleConstants {

	/**
	 * Private constructor to prevent instantiation
	 */
	private BrailleConstants() { }

	/**
	 * String containing the 64 braille patterns in 6 dot braille in Unicode order
	 */
	public static final String BRAILLE_PATTERNS_64;
	/**
	 * String containing all 256 braille patterns in Unicode order
	 */
	public static final String BRAILLE_PATTERNS_256;

	static {
		StringBuilder tmp = new StringBuilder();
		for (int i=0; i<256; i++) {
			tmp.append((char)(0x2800+i));
		}
		BRAILLE_PATTERNS_64 = tmp.substring(0, 64);
		BRAILLE_PATTERNS_256 = tmp.toString();
	}

}
