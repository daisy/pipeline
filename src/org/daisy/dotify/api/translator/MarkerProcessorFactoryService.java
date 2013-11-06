package org.daisy.dotify.api.translator;


/**
 * <p>
 * Provides an interface for a MarkerProcessorFactory service. The purpose of
 * this interface is to expose an implementation of a MarkerProcessorFactory as
 * a service.
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
public interface MarkerProcessorFactoryService {

	/**
	 * Returns true if the marker processor factory supports the given
	 * specification.
	 * 
	 * @param locale
	 *            a valid locale as defined by IETF RFC 3066
	 * @param mode
	 *            the marker processor grade
	 * @return returns true if the marker processor factory supports the
	 *         specification
	 */
	public boolean supportsSpecification(String locale, String mode);

	public MarkerProcessorFactory newFactory();

}