package org.daisy.dotify.api.engine;

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
	 * @throws FormatterEngineConfigurationException if a reference of type T cannot be bound to the implementation 
	 */
	public <T> void setReference(Class<T> c, T reference) throws FormatterEngineConfigurationException;

}