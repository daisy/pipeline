package org.daisy.dotify.api.table;

import java.nio.charset.Charset;

/**
 * Provides an interface for converting from text to braille and vice verca.
 * @author Joel Håkansson
 *
 */
public interface BrailleConverter {
	/**
	 * Transcodes the given text string as braille. This may be a one-to-one mapping or
	 * a many-to-one depending on the table implementation.
	 * @param text the text to convert
	 * @return returns a Unicode string of braille
	 */
	public String toBraille(String text);

	/**
	 * Transcodes the given braille into text.
	 *
	 * In most cases this will reverse the effect of
	 * toBraille(String text), i.e. text.equals(toText(toBraille(text))), however
	 * an implementation cannot rely on it.
	 *
	 * Values must be between 0x2800 and 0x28FF.
	 * @param braille the braille to convert
	 * @return returns a text string
	 */
	public String toText(String braille);

	/**
	 * Gets the preferred charset for this braille format when reading/writing as text from/to file
	 * @return returns the preferred charset
	 */
	public Charset getPreferredCharset();

	/**
	 * Returns true if 8-dot braille is supported, false otherwise
	 * @return returns true if 8-dot braille is supported, false otherwise
	 */
	public boolean supportsEightDot();
}
