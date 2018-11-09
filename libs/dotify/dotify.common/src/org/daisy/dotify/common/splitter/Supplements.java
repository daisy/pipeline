package org.daisy.dotify.common.splitter;

/**
 * Provides an interface for getting supplementary units.
 * @author Joel Håkansson
 *
 * @param <T> the type of units
 */
public interface Supplements<T> {

	/**
	 * Gets the unit for the specified Id.
	 * @param id the identifier for the unit
	 * @return returns the unit, if it exists, null otherwise
	 */
	public T get(String id);
	
	/**
	 * Gets the overhead associated with adding at least one supplement to the result.
	 * 
	 * @return returns the overhead associated with adding at least one supplement.
	 */
	public default double getOverhead() {
		return 0;
	}
}
