package org.daisy.dotify.api.hyphenator;


/**
 * <p>
 * Provides an interface for a HyphenatorFactory service. The purpose of this
 * interface is to expose an implementation of a HyphenatorFactory as a service.
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
public interface HyphenatorFactoryService {

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

	public HyphenatorFactory newFactory();

}