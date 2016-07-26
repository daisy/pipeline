package org.daisy.dotify.api.writer;

import java.util.Collection;




/**
 * <p>
 * Provides an interface for a PagedMediaWriterFactoryMaker service. The purpose of
 * this interface is to expose an implementation of a PagedMediaWriterFactoryMaker
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
public interface PagedMediaWriterFactoryMakerService {

	/**
	 * Gets a PagedMediaWriterFactory that supports the specified media type
	 * 
	 * @param mediaType
	 *            the target Internet media type
	 * @return returns a integer2text factory for the specified media type
	 * @throws PagedMediaWriterConfigurationException
	 *             if the media type is not supported
	 */
	public PagedMediaWriterFactory getFactory(String mediaType) throws PagedMediaWriterConfigurationException;

	/**
	 * Creates a new PagedMediaWriter. This is a convenience method for
	 * getFactory(target).newPagedMediaWriter(target).
	 * Using this method excludes the possibility of setting features of the
	 * PagedMediaWriter factory.
	 * 
	 * @param mediaType
	 *            the target Internet media type
	 * @return returns a new PagedMediaWriter
	 * @throws PagedMediaWriterConfigurationException
	 *             if the media type is not supported
	 */
	public PagedMediaWriter newPagedMediaWriter(String mediaType) throws PagedMediaWriterConfigurationException;
	
	/**
	 * Returns a list of supported Internet media types.
	 * @return returns a list of media types
	 */
	public Collection<String> listMediaTypes();

}
