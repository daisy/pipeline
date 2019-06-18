package org.daisy.dotify.api.translator;


/**
 * Provides an interface to a marker processor. A marker processor is
 * responsible for converting and inserting the supplied
 * text attributes as text markers.
 * 
 * @deprecated This class has been replaced by functionality supplied by {@link TranslatableWithContext}.
 * @author Joel Håkansson
 */
@Deprecated
public interface MarkerProcessor {

	/**
	 * Processes the input text and attributes into a text containing
	 * markers at the appropriate positions. The length of the text(s)
	 * must match the text attributes specified width.
	 * 
	 * @param atts
	 *            the text attributes that apply to the text.
	 * @param text
	 *            the text(s) to process
	 * @return returns a string with markers
	 * @throws IllegalArgumentException
	 *             if the specified attributes does not
	 *             match the text.
	 */
	public String processAttributes(TextAttribute atts, String... text);

	/**
	 * Processes the input text chunks and attributes into a text containing
	 * markers at the appropriate positions while retaining the text
	 * partition as specified by the input array. The length of the texts
	 * must match the text attributes specified width.
	 * 
	 * @param text
	 *            the texts to process
	 * @param atts
	 *            the text attributes that apply to the text.
	 * 
	 * @return returns an array of strings with markers
	 * 
	 * @throws IllegalArgumentException
	 *             if the specified attributes does not
	 *             match the text.
	 */
	public String[] processAttributesRetain(TextAttribute atts, String[] text);

}
