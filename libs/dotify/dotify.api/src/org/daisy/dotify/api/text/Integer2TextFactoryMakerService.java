package org.daisy.dotify.api.text;

import java.util.Collection;




/**
 * <p>
 * Provides an interface for a Integer2TextFactoryMaker service. The purpose of
 * this interface is to expose an implementation of a Integer2TextFactoryMaker
 * as an OSGi service.
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
public interface Integer2TextFactoryMakerService {

	/**
	 * Gets a Integer2TextFactory that supports the specified locale
	 * 
	 * @param locale
	 *            the target locale
	 * @return returns a integer2text factory for the specified locale
	 * @throws Integer2TextConfigurationException
	 *             if the locale is not supported
	 */
	public Integer2TextFactory getFactory(String locale) throws Integer2TextConfigurationException;

	/**
	 * Creates a new integer2text. This is a convenience method for
	 * getFactory(target).newInteger2Text(target).
	 * Using this method excludes the possibility of setting features of the
	 * integer2text factory.
	 * 
	 * @param locale
	 *            the target locale
	 * @return returns a new integer2text
	 * @throws Integer2TextConfigurationException
	 *             if the locale is not supported
	 */
	public Integer2Text newInteger2Text(String locale) throws Integer2TextConfigurationException;
	
	/**
	 * Returns a list of supported locales as defined by IETF RFC 3066.
	 * @return returns a list of locales
	 */
	public Collection<String> listLocales();

}
