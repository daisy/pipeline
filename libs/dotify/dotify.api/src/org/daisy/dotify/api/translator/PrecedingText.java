package org.daisy.dotify.api.translator;

/**
 * Provides information about an already processed item.
 * Note that what's provided through this interface is
 * the input to the process.
 * 
 * @author Joel Håkansson
 * @see ResolvableText
 */
public interface PrecedingText extends TextProperties {

	/**
	 * Resolves the item. Once the value has been resolved,
	 * it cannot change between calls in the same context.
	 * 
	 * @return the value, never null
	 */
	public String resolve();
}
