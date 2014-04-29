package org.daisy.dotify.api.writer;

import java.util.Collection;


/**
 * <p>
 * Provides an interface for a PagedMediaWriterFactory service. The purpose of this
 * interface is to expose an implementation of a PagedMediaWriterFactory as a
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
public interface PagedMediaWriterFactoryService {

	/**
	 * Returns true if this instance can create instances for the specified
	 * mediaType.
	 * 
	 * @param mediaType
	 *            a valid Internet media type
	 * @return returns true if the specified locale is supported, false
	 *         otherwise
	 */
	public boolean supportsMediaType(String mediaType);
	
	/**
	 * Returns a list of supported locales as defined by IETF RFC 3066.
	 * @return returns a list of locales
	 */
	public Collection<String> listMediaTypes();

	public PagedMediaWriterFactory newFactory(String mediaType);

}