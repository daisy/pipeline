package org.daisy.dotify.impl.system.common;

import java.net.URL;

import org.daisy.dotify.common.io.AbstractResourceLocator;

/**
 * Provides a method to find resources relative to this class. No common
 * resources are
 * avaliable at this moment.
 * 
 * @author Joel Håkansson
 * 
 */
public class CommonResourceLocator extends AbstractResourceLocator {
	/**
	 * Provides identifiers that can be used to locate resources
	 * maintained by this class.
	 */
	public enum CommonResourceIdentifier {

	}
	private static CommonResourceLocator instance;
	
	private CommonResourceLocator() {
		super();
	}
	
	/**
	 * Gets the instance of the Obfl resource locator if it exists, or creates
	 * it if it does not (singleton).
	 * @return returns the instance
	 */
	public static synchronized CommonResourceLocator getInstance() {
		if (instance==null) {
			instance = new CommonResourceLocator();
		}
		return instance;
	}
	
	/**
	 * Gets a resource by identifier. It is preferred to use this method 
	 * rather than get a resource by string, since the internal structure
	 * of this package should be considered opaque to users of this class.
	 * @param identifier the identifier of the resource to get.
	 * @return returns the URL to the resource
	 */
	public URL getResourceByIdentifier(CommonResourceIdentifier identifier) {
		switch (identifier) {
			default:
				throw new RuntimeException("Enum identifier not implemented. This is a coding error.");
		}
	}
}
