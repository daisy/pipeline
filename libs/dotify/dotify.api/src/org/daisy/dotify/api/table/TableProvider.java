package org.daisy.dotify.api.table;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.factory.Provider;

/**
 * <p>
 * Provides an interface for a Table service. The purpose of this
 * interface is to expose an implementation of Table as a
 * service.
 * </p>
 *
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 *
 * @author Joel Håkansson
 */
public interface TableProvider extends Provider<FactoryProperties> {

	/**
	 * Creates a new table with the specified identifier.
	 * @param identifier the identifier
	 * @return returns a new table
	 */
	public Table newFactory(String identifier);
}
