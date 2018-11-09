package org.daisy.dotify.api.factory;

/**
 * Provides an interface for filtering a collection of Factories.
 * @author Joel Håkansson
 *
 */
public interface FactoryFilter {

	/**
	 * Tests if a specified object should be included in a list.
	 * @param object the Object to test
	 * @return returns true if the specified object should be included in a list, false otherwise
	 */
	public boolean accept(FactoryProperties object);

}
