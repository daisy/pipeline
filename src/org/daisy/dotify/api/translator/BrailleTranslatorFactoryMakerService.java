package org.daisy.dotify.api.translator;

public interface BrailleTranslatorFactoryMakerService {

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
	 * Gets a factory for the given specification.
	 * 
	 * @param locale
	 *            the locale for the factory
	 * @param grade
	 *            the grade for the factory
	 * @return returns a braille translator factory
	 * @throws TranslatorConfigurationException
	 *             if the specification is not supported
	 */
	public BrailleTranslatorFactory newFactory(String locale, String grade) throws TranslatorConfigurationException;

}
