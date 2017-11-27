package org.daisy.dotify.consumer.obfl;

import java.util.ServiceLoader;

import org.daisy.dotify.api.obfl.ObflParserFactoryService;

/**
 * <p>Like all classes in the org.daisy.dotify.consumer sub packages, this
 * class is only used directly in SPI context. Unlike some other classes however,
 * this class does not implement a service interface that can be used from
 * OSGi. The reason for this is that the implementation <i>simply returns
 * a single instance of the lower level interface</i> with references populated
 * with SPI. To use in OSGi context, request the lower level service directly
 * from the DS registry.</p>
 * 
 * @author Joel Håkansson
 * @deprecated use the corresponding api class
 */
@Deprecated
public class ObflParserFactoryMaker {
	private ObflParserFactoryService proxy;

	/**
	 * Creates a new instance.
	 */
	public ObflParserFactoryMaker() {
		super();
	}
	
	/**
	 * Gets the OBFL parser factory service.
	 * @return returns the OBFL parser factory service
	 */
	public ObflParserFactoryService getFactory() {
		return proxy;
	}

	/**
	 * Creates a new OBFL parser factory maker instance.
	 * @return returns a new instance using spi
	 */
	public static ObflParserFactoryMaker newInstance() {
		ObflParserFactoryMaker ret = new ObflParserFactoryMaker();
		// Gets the first obfl parser factory (assumes there is at least one).
		ret.proxy = ServiceLoader.load(ObflParserFactoryService.class).iterator().next();
		ret.proxy.setCreatedWithSPI();
		return ret;
	}

}