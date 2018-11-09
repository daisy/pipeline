package org.daisy.dotify.api.table;

import org.daisy.dotify.api.factory.Factory;

/**
 * Provides an interface for common properties of a Table.
 * @author Joel Håkansson
 *
 */
public interface Table extends Factory {
	/**
	 * Creates a new BrailleConverter based on the current configuration of this
	 * Table.
	 * @return returns a new BrailleConverter instance
	 */
	public BrailleConverter newBrailleConverter();
}
