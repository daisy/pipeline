package org.daisy.dotify.api.formatter;

/**
 * Provides an interface for fallback rules.
 * @author Joel Håkansson
 *
 */
public interface FallbackRule {
	/**
	 * Gets the identifier of the collection that this rule should be applied to.
	 * 
	 * @return returns the identifier
	 */
	public String applyToCollection();
	
	/**
	 * Returns true of the rule can only be applied to the context collection,
	 * in other words the collection that triggers the fallback actions.
	 *  
	 * @return returns true if the rule can only be applied to the context 
	 * collection, false otherwise
	 */
	public boolean mustBeContextCollection();
}
