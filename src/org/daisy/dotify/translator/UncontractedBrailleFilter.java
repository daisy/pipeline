package org.daisy.dotify.translator;

import org.daisy.dotify.text.StringFilter;



/**
 * <p>Provides a filter for uncontracted braille. Implementations 
 * are responsible for converting any input to uncontracted unicode 
 * braille. Characters that:</p>
 * <ul>
 * <li>represent a word boundary or a hyphenation point and</li>
 * <li>has a one-to-one character mapping to braille</li>
 * </ul>
 * <p>may be excluded, for example soft hyphen (0x00ad), dash (0x002d) and space (0x00a0, 0x0020).
 * Purposely excluded characters should be translated in the finalize method.</p>
 * @author Joel Håkansson
 */
public interface UncontractedBrailleFilter extends StringFilter {
	
	public void setLocale(String locale);

	public boolean supportsLocale(String locale);
	
	/**
	 * Finalizes braille translation, replacing remaining non braille characters
	 * with braille characters. An implementation can assume that the input has been
	 * filtered. The resulting string must have the same length as the input string.
	 * @param input the input string, mostly braille
	 * @return returns the finalized string
	 */
	public String finalize(String input);

}
