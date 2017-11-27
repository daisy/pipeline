package org.daisy.dotify.consumer.obfl;

import java.util.ServiceLoader;

import org.daisy.dotify.api.obfl.ExpressionFactory;

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
public class ExpressionFactoryMaker {
	private final ExpressionFactory proxy;

	/**
	 * Creates a new expression factory maker
	 */
	public ExpressionFactoryMaker() {
		// Gets the first formatter engine (assumes there is at least one).
		proxy = ServiceLoader.load(ExpressionFactory.class).iterator().next();
		proxy.setCreatedWithSPI();
	}

	/**
	 * Creates a new expression factory maker instance
	 * @return returns a new expression factory maker instance
	 */
	public static ExpressionFactoryMaker newInstance() {
		return new ExpressionFactoryMaker();
	}

	/**
	 * Gets an expression factory.
	 * @return returns an expression factory
	 */
	public ExpressionFactory getFactory() {
		return proxy;
	}

}
