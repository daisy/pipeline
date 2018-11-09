package org.daisy.dotify.api.translator;




/**
 * <p>
 * Provides an interface for a MarkerProcessorFactoryMaker service. The purpose
 * of this interface is to expose an implementation of a
 * MarkerProcessorFactoryMaker as an OSGi service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 * 
 */
public interface MarkerProcessorFactoryMakerService {

	/**
	 * Returns true if the specification is supported, false otherwise
	 * @param locale the locale
	 * @param grade the braille grade
	 * @return returns true if the specification is supported, false otherwise
	 */
	public boolean supportsSpecification(String locale, String grade);

	/**
	 * Gets a factory for the given specification.
	 * 
	 * @param locale
	 *            the locale for the factory
	 * @param grade
	 *            the grade for the factory
	 * @return returns a marker processor factory
	 * @throws MarkerProcessorConfigurationException
	 *             if the specification is not supported
	 */
	public MarkerProcessorFactory newFactory(String locale, String grade) throws MarkerProcessorConfigurationException;

	/**
	 * Creates a new marker processor with the suppled specification.
	 * @param locale the locale
	 * @param grade the braille grade
	 * @return returns a new marker processor
	 * @throws MarkerProcessorConfigurationException if no processor could be created
	 */
	public MarkerProcessor newMarkerProcessor(String locale, String grade) throws MarkerProcessorConfigurationException;
}
