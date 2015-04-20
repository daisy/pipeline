package org.daisy.dotify.api.translator;

/**
 * Provides a braille translator result.
 * @author Joel Håkansson
 *
 */
public interface BrailleTranslatorResult {
	
	/**
	 * Gets the translated string preceding the row break, including a translated 
	 * hyphen at the end, if needed. The length of the translated text must not exceed the
	 * specified <tt>limit</tt>. If <tt>force</tt> is not used, the result could be empty and no 
	 * characters removed from the buffer. A caller would typically set <tt>force</tt> to 
	 * true when <tt>limit</tt> is equal to the maximum number of available characters 
	 * on a row.
	 * 
	 * @param limit specifies the maximum number of characters allowed in the result
	 * @param force specifies if the translator should force a break at the limit
	 * 				 if no natural break point is found 
	 * @return returns the translated string
	 */
	public String nextTranslatedRow(int limit, boolean force);
	
	/**
	 * Gets the translated remainder, in other words the characters not
	 * yet extracted with <tt>nextTranslatedRow</tt>.
	 * @return returns the translated remainder
	 */
	public String getTranslatedRemainder();
	
	/**
	 * Returns the number of characters remaining in the result. This
	 * number equals the number of characters in <tt>getTranslatedRemainder</tt>.
	 * @return returns the number of characters remaining
	 */
	public int countRemaining();

	/**
	 * Returns true if there are characters remaining in the result, in other
	 * words of there are characters not yet extracted with <tt>nextTranslatedRow</tt>.
	 * @return returns true if there are characters remaining, false otherwise
	 */
	public boolean hasNext();

}
