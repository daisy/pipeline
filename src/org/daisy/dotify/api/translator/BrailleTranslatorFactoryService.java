package org.daisy.dotify.api.translator;

import java.util.Collection;

/**
 * <p>
 * Provides an interface for a BrailleTranslatorFactory service. The purpose of
 * this interface is to expose an implementation of a BrailleTranslatorFactory as
 * a service.
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
public interface BrailleTranslatorFactoryService {

	/**
	 * Returns true if the translator factory supports the given specification.
	 * 
	 * @param locale
	 *            the translator locale
	 * @param mode
	 *            the translator grade, or null for uncontracted braille
	 * @return returns true if the translator factory supports the specification
	 */
	public boolean supportsSpecification(String locale, String mode);
	
	/**
	 * Returns a list of supported specifications.
	 * @return returns a list of specifications
	 */
	public Collection<TranslatorSpecification> listSpecifications();

	public BrailleTranslatorFactory newFactory();

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
	 * @throws TranslatorConfigurationException if a reference of type T cannot be bound to the implementation 
	 */
	public <T> void setReference(Class<T> c, T reference) throws TranslatorConfigurationException;

}
