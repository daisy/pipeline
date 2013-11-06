package org.daisy.dotify.api.translator;

/**
 * Provides an interface for text attributes.
 * 
 * @author Joel Håkansson
 * 
 */
public interface TextAttribute extends Iterable<TextAttribute> {

	/**
	 * Gets the width of this object
	 * 
	 * @return returns the width, in characters
	 */
	public int getWidth();

	/**
	 * Gets the dictionary identifier that applies to this text
	 * attribute.
	 * 
	 * @return returns the dictionary identifier, or null if it does not have
	 *         any
	 */
	public String getDictionaryIdentifier();

	/**
	 * Returns true if this object has text attribute children.
	 * 
	 * @return returns true if this object has a text attribute children, false
	 *         otherwise
	 */
	public boolean hasChildren();

}
