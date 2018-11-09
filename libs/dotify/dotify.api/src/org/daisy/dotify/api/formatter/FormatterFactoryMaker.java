package org.daisy.dotify.api.formatter;

import java.util.ServiceLoader;

/**
 * <p>Provides a factory for formatters. The factory will instantiate 
 * the first Formatter it encounters when querying the services API.</p> 

 * <p>Like all maker classes in the api, this
 * class is only used directly in SPI context. Unlike some other classes however,
 * this class does not implement a service interface that can be used from
 * OSGi. The reason for this is that the implementation <i>simply returns
 * a single instance of the lower level interface</i> with references populated
 * with SPI. To use in OSGi context, request the lower level service directly
 * from the DS registry.</p>
 * 
 * @author Joel Håkansson
 */
//TODO: deprecate or add service layer
//See: https://github.com/joeha480/dotify/issues/161
public class FormatterFactoryMaker {
	private final FormatterFactory proxy;
	
	/**
	 * Creates a new formatter factory maker
	 */
	public FormatterFactoryMaker() {
		//Gets the first formatter (assumes there is at least one).
		proxy = ServiceLoader.load(FormatterFactory.class).iterator().next();
		proxy.setCreatedWithSPI();
	}

	/**
	 * Creates a new instance of a formatter factory maker
	 * @return returns a new formatter factory maker
	 */
	public static FormatterFactoryMaker newInstance() {
		return new FormatterFactoryMaker();
	}
	
	/**
	 * Gets a formatter factory
	 * @return returns a formatter factory
	 */
	public FormatterFactory getFactory() {
		return proxy;
	}
	
	/**
	 * Creates a new formatter with the specified options.
	 * @param locale the locale
	 * @param mode the braille mode
	 * @return returns a new formatter
	 */
	public Formatter newFormatter(String locale, String mode) {
		return proxy.newFormatter(locale, mode);
	}
}
