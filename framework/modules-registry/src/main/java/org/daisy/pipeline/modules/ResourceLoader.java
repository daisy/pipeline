package org.daisy.pipeline.modules;

import java.net.URL;
import java.nio.file.NoSuchFileException;

/**
 * The Interface ResourceLoader allows to get an accessible URI from the path provided. This is used for loading {@link Component} objects.
 */
public interface ResourceLoader {

	/**
	 * Loads the resource.
	 *
	 * @param path the (not URL-encoded) path, relative to catalog.xml
	 * @return An encoded absolute URL
	 * @throws NoSuchFileException if the resource is not available
	 */
	URL loadResource(String path) throws NoSuchFileException;

	/**
	 * Loads a list of resources recursively.
	 */
	Iterable<URL> loadResources(String path);

}
