package org.daisy.dotify.impl.translator.liblouis;


public interface ResourceResolver {
	

	/**
	 * Resolves relative resources. 
	 */
	public ResourceDescriptor resolve(String path);

}
