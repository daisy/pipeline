package org.daisy.dotify.impl.input.epub;

import org.daisy.dotify.common.io.AbstractResourceLocator;

/**
 * Provides a resorce locator relative to this class.
 * @author Joel Håkansson
 */
public class EpubResourceLocator extends AbstractResourceLocator {

	/**
	 * Creates a new resource locator for the specified relative path.
	 * @param subpath the relative path
	 */
	public EpubResourceLocator(String subpath) {
		super(subpath);
	}

}
