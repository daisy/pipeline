package org.daisy.dotify.api.translator;

/**
 * Provides a factory for braille filters.
 * @author Joel Håkansson
 *
 */
public interface BrailleFilterFactory {
	
	/**
	 * Creates a new filter with the given specification
	 * @param locale the filter locale
	 * @param mode the filter grade
	 * @return returns a new braille filter
	 * @throws TranslatorConfigurationException if the specification is not supported
	 */
	public BrailleFilter newFilter(String locale, String mode) throws TranslatorConfigurationException;

}
