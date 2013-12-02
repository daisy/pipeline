package org.daisy.dotify.api.engine;

import org.daisy.dotify.api.writer.PagedMediaWriter;

public interface FormatterEngineFactoryService {

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