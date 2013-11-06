package org.daisy.dotify.api.formatter;


/**
 * Provides an interface for cross references, that is to say
 * the location of a specific identifier.
 * @author Joel Håkansson
 *
 */
public interface CrossReferences {
	
	/**
	 * Gets the page number for the specified identifier.
	 * @param refid the identifier to get the page for
	 * @return returns the page number, one-based
	 */
	public Integer getPageNumber(String refid);
	
	/**
	 * Gets the volume for the specified identifier.
	 * @param refid the identifier to get the volume for
	 * @return returns the volume number, one-based
	 */
	public Integer getVolumeNumber(String refid);

}
