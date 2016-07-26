package org.daisy.dotify.api.translator;

import java.util.Collection;

/**
 * <p>
 * Provides an interface for a BrailleFilterFactoryMaker service. The purpose of
 * this interface is to expose an implementation of a BrailleFilterFactoryMaker as
 * an OSGi service.
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
public interface BrailleFilterFactoryMakerService {

	/**
	 * Returns true if the filter factory supports the given specification.
	 * 
	 * @param locale
	 *            the filter locale
	 * @param mode
	 *            the filter grade, or null for uncontracted braille
	 * @return returns true if the filter factory supports the specification
	 */
	public boolean supportsSpecification(String locale, String mode);

	/**
	 * Returns a list of supported specifications.
	 * @return returns a list of specifications
	 */
	public Collection<TranslatorSpecification> listSpecifications();

	/**
	 * Gets a factory for the given specification.
	 * 
	 * @param locale
	 *            the locale for the factory
	 * @param grade
	 *            the grade for the factory
	 * @return returns a braille filter factory
	 * @throws TranslatorConfigurationException
	 *             if the specification is not supported
	 */
	public BrailleFilterFactory newFactory(String locale, String grade) throws TranslatorConfigurationException;
	
	/**
	 * Gets a filter for the given specification
	 * @param locale the locale for the filter
	 * @param grade the grade for the filter
	 * @return returns a braille filter
	 * @throws TranslatorConfigurationException if the specification is not supported
	 */
	public BrailleFilter newFilter(String locale, String grade) throws TranslatorConfigurationException;

}
