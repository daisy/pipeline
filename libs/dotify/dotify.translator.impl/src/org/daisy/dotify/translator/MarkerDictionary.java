package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.TextAttribute;



/**
 * Provides an interface for marker dictionaries. A
 * marker dictionary resolves text markers based on the
 * contents of the supplied string.
 * 
 * @author Joel Håkansson
 * 
 */
@FunctionalInterface
public interface MarkerDictionary {

	/**
	 * Gets the markers that apply to the specified string.
	 * 
	 * @param str
	 *            the string to find markers for
	 * @param attributes
	 *            the attributes for the string
	 * @return returns markers that apply to the input
	 * @throws MarkerNotFoundException
	 *             if no markers apply to the supplied string
	 * @throws MarkerNotCompatibleException
	 *             if the markers cannot be applied to the attribute structure
	 */
	public Marker getMarkersFor(String str, TextAttribute attributes) throws MarkerNotFoundException, MarkerNotCompatibleException;

}