package org.daisy.dotify.api.hyphenator;

import java.util.Collection;


/**
 * <p>
 * Provides an interface for a HyphenatorFactoryMaker service. The purpose of
 * this interface is to expose an implementation of a HyphenatorFactoryMaker as
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
public interface HyphenatorFactoryMakerService {

    /**
     * Gets a HyphenatorFactory that supports the specified locale.
     *
     * @param target the target locale
     * @return returns a hyphenator factory for the specified locale
     * @throws HyphenatorConfigurationException if the locale is not supported
     */
    public HyphenatorFactory newFactory(String target) throws HyphenatorConfigurationException;

    /**
     * Creates a new hyphenator. This is a convenience method for
     * getFactory(target).newHyphenator(target).
     * Using this method excludes the possibility of setting features of the
     * hyphenator factory.
     *
     * @param target the target locale
     * @return returns a new hyphenator
     * @throws HyphenatorConfigurationException if the locale is not supported
     */
    public HyphenatorInterface newHyphenator(String target) throws HyphenatorConfigurationException;


    /**
     * Returns a list of supported locales as defined by IETF RFC 3066.
     *
     * @return returns a list of locales
     */
    public Collection<String> listLocales();
}
