package org.daisy.dotify.consumer.formatter;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.consumer.translator.BrailleTranslatorFactoryMaker;

/**
 * Provides a factory for formatters. The factory will instantiate 
 * the first Formatter it encounters when querying the services API. 
 * 
 * @author Joel Håkansson
 */
public class FormatterFactoryMaker {
	private final FormatterFactory proxy;
	
	public FormatterFactoryMaker() {
		//Gets the first formatter (assumes there is at least one).
		proxy = ServiceRegistry.lookupProviders(FormatterFactory.class).next();
		proxy.setTranslator(BrailleTranslatorFactoryMaker.newInstance());
	}

	public static FormatterFactoryMaker newInstance() {
		return new FormatterFactoryMaker();
	}
	
	public FormatterFactory getFactory() {
		return proxy;
	}
	
	public Formatter newFormatter(String locale, String mode) {
		return proxy.newFormatter(locale, mode);
	}
}
