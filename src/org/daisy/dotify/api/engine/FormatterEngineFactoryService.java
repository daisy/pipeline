package org.daisy.dotify.api.engine;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.writer.PagedMediaWriter;

/**
 * <p>
 * Provides an interface for a FormatterEngineFactory service. The purpose of this
 * interface is to expose an implementation of a FormaterEngine as a service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 */
public interface FormatterEngineFactoryService {

	/**
	 * Returns a new FormatterEngine configured for the specified locale, mode and output writer.
	 * 
	 * @param locale the locale
	 * @param mode the braille mode
	 * @param writer the output writer
	 * 
	 * @return returns a new FormatterEngine
	 */
	public FormatterEngine newFormatterEngine(String locale, String mode, PagedMediaWriter writer);
	
	/**
	 * Returns a new FormatterEngine configured with the specified configuration and output writer
	 * @param config the configuration
	 * @param writer the output writer
	 * @return returns a new instance
	 */
	public FormatterEngine newFormatterEngine(FormatterConfiguration config, PagedMediaWriter writer);

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