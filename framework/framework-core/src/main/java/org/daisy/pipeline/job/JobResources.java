package org.daisy.pipeline.job;

import java.io.InputStream;

import com.google.common.base.Supplier;


// TODO: Auto-generated Javadoc
/**
 * The Interface ResourceCollection defines a set of resources and methods to access them.
 */
public interface JobResources {

	/**
	 * Gets the names.
	 *
	 * @return the names
	 */
	Iterable<String> getNames();

	/**
	 * Gets the resource.
	 *
	 * @param name the name
	 * @return the resource
	 */
	Supplier<InputStream> getResource(String name);
}
