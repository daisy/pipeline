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
	 * <p>Informs the implementation that it was discovered and instantiated using
	 * information collected from a file within the <tt>META-INF/services</tt> directory.
	 * In other words, it was created using SPI (service provider interfaces).</p>
	 * 
	 * <p>This information, in turn, enables the implementation to use the same mechanism
	 * to set dependencies as needed.</p>
	 * 
	 * <p>If this information is <strong>not</strong> given, an implementation
	 * should avoid using SPIs and instead use
	 * <a href="http://wiki.osgi.org/wiki/Declarative_Services">declarative services</a>
	 * for dependency injection as specified by OSGi. Note that this also applies to
	 * several newInstance() methods in the Java API.</p>
	 * 
	 * <p>The class that created an instance with SPI must call this method before
	 * putting it to use.</p>
	 */
	public default void setCreatedWithSPI(){}

}