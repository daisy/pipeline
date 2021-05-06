package org.daisy.dotify.translator.impl.liblouis.java;


/**
 * Provides a resource resolver interface.
 *
 * @author Joel Håkansson
 */
@FunctionalInterface
public interface ResourceResolver {

    /**
     * Resolves relative resources.
     *
     * @param path the path to resolve
     * @return returns the resource descriptor
     */
    public ResourceDescriptor resolve(String path);

}
