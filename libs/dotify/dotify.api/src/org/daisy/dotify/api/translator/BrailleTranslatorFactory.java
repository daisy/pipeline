package org.daisy.dotify.api.translator;

/**
 * Provides a factory for braille translation and hyphenation.
 * @author Joel Håkansson
 *
 */
public interface BrailleTranslatorFactory {
	/**
	 * Defines bypass mode
	 */
	public static final String MODE_BYPASS = "bypass";
	/**
	 * Defines uncontracted mode
	 */
	public static final String MODE_UNCONTRACTED = "uncontracted";
	
	/**
	 * Creates a new translator with the given specification
	 * @param locale the translator locale
	 * @param mode the translator grade
	 * @return returns a new translator
	 * @throws TranslatorConfigurationException if the specification is not supported
	 */
	public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException;

}
