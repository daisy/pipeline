package org.daisy.dotify.api.text;

import java.util.Collection;


/**
 * <p>
 * Provides an interface for a Integer2TextFactory service. The purpose of this
 * interface is to expose an implementation of a Integer2TextFactory as a
 * service.
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
public interface Integer2TextFactoryService {

	/**
	 * Returns true if this instance can create instances for the specified
	 * locale.
	 * 
	 * @param locale
	 *            a valid locale as defined by IETF RFC 3066
	 * @return returns true if the specified locale is supported, false
	 *         otherwise
	 */
	public boolean supportsLocale(String locale);
	
	/**
	 * Returns a list of supported locales. If any locale is supported,
	 * a list containing the single entry "*" is allowed.
	 * @return returns a list of locales
	 */
	public Collection<String> listLocales();

	public Integer2TextFactory newFactory();

}