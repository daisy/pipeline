package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.TextAttribute;


/**
 * Provides a filter for text attributes. 
 * @author Joel Håkansson
 *
 */
@FunctionalInterface
public interface TextAttributeFilter {

	/**
	 * Returns true if the filter allows the specified text attributes, false
	 * otherwise.
	 * 
	 * @param atts
	 *            the attributes to test
	 * @return returns true if the filter allows the specified text attributes,
	 *         false otherwise
	 */
	public boolean appliesTo(TextAttribute atts);
}
