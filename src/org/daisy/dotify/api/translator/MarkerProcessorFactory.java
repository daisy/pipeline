package org.daisy.dotify.api.translator;

/**
 * Provides a factory for braille markers.
 * 
 * @author Joel Håkansson
 * 
 */
public interface MarkerProcessorFactory {

	/**
	 * Creates a new marker processor with the given specification
	 * @param locale the marker processor locale
	 * @param mode the marker processor grade
	 * @return returns a new marker processor
	 * @throws TranslatorConfigurationException if the specification is not supported
	 */
	public MarkerProcessor newMarkerProcessor(String locale, String mode) throws MarkerProcessorConfigurationException;

}
