package org.daisy.dotify.api.translator;

import java.util.Collection;

/**
 * <p>
 * Provides an interface for a BrailleTranslatorFactoryMaker service. The purpose of
 * this interface is to expose an implementation of a BrailleTranslatorFactoryMaker as
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
public interface BrailleTranslatorFactoryMakerService {

    /**
     * Returns true if the translator factory supports the given specification.
     *
     * @param locale the translator locale
     * @param mode   the translator grade, or null for uncontracted braille
     * @return returns true if the translator factory supports the specification
     */
    public boolean supportsSpecification(String locale, String mode);

    /**
     * Returns a list of supported specifications.
     *
     * @return returns a list of specifications
     */
    public Collection<TranslatorSpecification> listSpecifications();

    /**
     * Gets a factory for the given specification.
     *
     * @param locale the locale for the factory
     * @param grade  the grade for the factory
     * @return returns a braille translator factory
     * @throws TranslatorConfigurationException if the specification is not supported
     */
    public BrailleTranslatorFactory newFactory(String locale, String grade) throws TranslatorConfigurationException;

    /**
     * Gets a translator for the given specification.
     *
     * @param locale the locale for the translator
     * @param grade  the grade for the translator
     * @return returns a braille translator
     * @throws TranslatorConfigurationException if the specification is not supported
     */
    public BrailleTranslator newTranslator(String locale, String grade) throws TranslatorConfigurationException;

}
