package org.daisy.dotify.consumer.obfl;

import java.util.ServiceLoader;

import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.consumer.text.Integer2TextFactoryMaker;

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
 */
public class ExpressionFactoryMaker {
	private final ExpressionFactory proxy;

	public ExpressionFactoryMaker() {
		// Gets the first formatter engine (assumes there is at least one).
		proxy = ServiceLoader.load(ExpressionFactory.class).iterator().next();
		// populate the engine factory with SPI here as this class is never used
		// from OSGi
		proxy.setInteger2TextFactory(Integer2TextFactoryMaker.newInstance());
	}

	public static ExpressionFactoryMaker newInstance() {
		return new ExpressionFactoryMaker();
	}

	public ExpressionFactory getFactory() {
		return proxy;
	}

}
