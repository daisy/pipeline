package org.daisy.dotify.api.translator;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;

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
	 * Provides a method to set the HyphenatorFactoryMakerService directly. This
	 * is included in the interface as a compromise between OSGi visibility and
	 * SPI compatibility.
	 * 
	 * In an OSGi context, the implementation should not set the implementation
	 * directly, but attach it to the service using DS.
	 * 
	 * @param hyphenator
	 */
	public void setHyphenator(HyphenatorFactoryMakerService hyphenator);
}
