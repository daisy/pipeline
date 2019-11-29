package org.daisy.dotify.api.translator;

/**
 * Provides a factory for braille translation and hyphenation.
 * @author Joel Håkansson
 *
 */
public interface BrailleTranslatorFactory {
	
	/**
	 * Creates a new translator with the given specification
	 * @param locale the translator locale
	 * @param mode the translator grade
	 * @return returns a new translator
	 * @throws TranslatorConfigurationException if the specification is not supported
	 */
	public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException;

}
