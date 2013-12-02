package org.daisy.dotify.api.translator;


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
