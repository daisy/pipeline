package org.daisy.dotify.common.layout;

/**
 * Provides an interface for getting supplementary units.
 * @author Joel Håkansson
 *
 * @param <T> the type of units
 * @deprecated use the corresponding class in the org.daisy.dotify.common.split package.
 */
@Deprecated
public interface Supplements<T> {

	/**
	 * Gets the unit for the specified Id.
	 * @param id the identifier for the unit
	 * @return returns the unit, if it exists, null otherwise
	 */
	public T get(String id);
}
