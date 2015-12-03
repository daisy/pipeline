package org.daisy.dotify.api.formatter;



/**
 * Provides a proxy for creating a formatter implementation. Objects of this class
 * are detected by the formatter factory and their sole purpose is to create
 * instances of a formatter implementation.
 * @author Joel Håkansson
 */
public interface FormatterFactory {
	
	/**
	 * Creates a new formatter.
	 * @param locale the locale
	 * @param mode the mode
	 * @return returns the new formatter.
	 */
	public Formatter newFormatter(String locale, String mode);

	/**
	 * Provides a method to set references directly. This
	 * is included in the interface as a compromise between OSGi visibility and
	 * SPI compatibility.
	 * 
	 * In an OSGi context, the implementation should not set references
	 * directly, but attach it to the service using DS.
	 * 
	 * @param c the reference class
	 * @param reference the reference instance
	 * 
	 * @throws FormatterConfigurationException if a reference of type T cannot be bound to the implementation 
	 */
	public <T> void setReference(Class<T> c, T reference) throws FormatterConfigurationException;

}