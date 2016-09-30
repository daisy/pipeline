package org.daisy.dotify.common.io;

import java.net.URL;


/**
 * Provides a method for locating resources.
 * @author Joel Håkansson
 *
 */
public interface ResourceLocator {

	/**
	 * Gets the URL for the given resource path.
	 * @param subpath the path to the resource
	 * @return returns the URL for the given resource
	 * @throws ResourceLocatorException if the resource could not be found
	 */
	public URL getResource(String subpath) throws ResourceLocatorException;
}
