package org.daisy.dotify.tasks.impl.input.xml;

import org.daisy.dotify.common.io.AbstractResourceLocator;

/**
 * Provides a resource locator for resources in this package.
 * 
 * @author Joel Håkansson
 */
public class CommonResourceLocator extends AbstractResourceLocator {
	/**
	 * Creates a new common resource locator with the specified path 
	 * (relative to this class).
	 * 
	 * @param subpath the relative path
	 */
	public CommonResourceLocator(String subpath) {
		super(subpath);
	}
}