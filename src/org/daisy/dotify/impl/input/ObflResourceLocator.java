package org.daisy.dotify.impl.input;

import java.net.URL;

import org.daisy.dotify.common.io.AbstractResourceLocator;
import org.daisy.dotify.common.io.ResourceLocatorException;

/**
 * Provides a method to find resources related to OBFL
 * @author Joel Håkansson
 *
 */
public class ObflResourceLocator extends AbstractResourceLocator {
	/**
	 * Provides identifiers that can be used to locate specific resources
	 * maintained by this class.
	 */
	public enum ObflResourceIdentifier {
		/**
		 * A relax ng schema describing OBFL
		 */
		OBFL_RNG_SCHEMA,
		/**
		 * A nvdl schema describing OBFL
		 */
		OBFL_NVDL_SCHEMA
	}
	private static ObflResourceLocator instance;
	
	private ObflResourceLocator() {
		super();
	}
	
	/**
	 * Gets the instance of the Obfl resource locator if it exists, or creates
	 * it if it does not (singleton).
	 * @return returns the instance
	 */
	public static synchronized ObflResourceLocator getInstance() {
		if (instance==null) {
			instance = new ObflResourceLocator();
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
	public URL getResourceByIdentifier(ObflResourceIdentifier identifier) {
		try {
			switch (identifier) {
				case OBFL_RNG_SCHEMA:
					return getResource("resource-files/obfl.rng");
				case OBFL_NVDL_SCHEMA:
					return getResource("resource-files/obfl.nvdl");
				default:
					throw new RuntimeException("Enum identifier not implemented. This is a coding error.");
			}
		} catch (ResourceLocatorException e) {
			throw new RuntimeException("Could not locate resource by enum identifier. This is a coding error.", e);
		}
	}

}
