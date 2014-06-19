package org.daisy.dotify.consumer.obfl;

import java.util.ServiceLoader;

import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.consumer.text.Integer2TextFactoryMaker;

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
