package org.daisy.dotify.api.writer;


/**
 * Provides a PagedMediaWriter factory interface. This interface is used to retrieve
 * a PagedMediaWriter instance.
 * 
 * @author Joel Håkansson
 * 
 */
public interface PagedMediaWriterFactory {
	
	/**
	 * Returns a new PagedMediaWriter configured for the specified locale.
	 * 
	 * @param mediaType
	 *            the Internet media type for the new PagedMediaWriter
	 * @return returns a new PagedMediaWriter
	 * @throws PagedMediaWriterConfigurationException
	 *             if the media type is not supported
	 */
	public PagedMediaWriter newPagedMediaWriter() throws PagedMediaWriterConfigurationException;
	
	/**
	 * Gets the value of a PagedMediaWriter feature.
	 * 
	 * @param key
	 *            the feature to get the value for
	 * @return returns the value, or null if not set
	 */
	public Object getFeature(String key);
	
	/**
	 * Sets the value of a PagedMediaWriter feature.
	 * 
	 * @param key
	 *            the feature to set the value for
	 * @param value
	 *            the value for the feature
	 * @throws PagedMediaWriterConfigurationException
	 *             if the feature is not supported
	 */
	public void setFeature(String key, Object value) throws PagedMediaWriterConfigurationException;
}
