package org.daisy.dotify.api.factory;


/**
 * Provides an interface for factory catalogs.
 *
 * @param <T> the type of factory objects that this catalog contains
 * @author Joel Håkansson
 */
public interface FactoryCatalog<T extends Factory> {

    /**
     * Gets the Factory with this identifier.
     *
     * @param identifier the identifier for the requested Factory
     * @return returns the Factory with this identifier, or null if none is found
     */
    public T get(String identifier);

}
