package org.daisy.dotify.api.formatter;

/**
 * Provides a formatter context that can be used to process context
 * aware content.
 * @author Joel Håkansson
 *
 */
public interface Context {

	/**
	 * Gets the current volume number (one based) or null if not known.
	 * @return returns the current volume number
	 */
	public Integer getCurrentVolume();

	/**
	 * Gets the volume count or null if not known.
	 * @return returns the volume count
	 */
	public Integer getVolumeCount();

	/**
	 * Gets the current page number (one based) or null if not known.
	 * @return returns the current page number
	 */
	public Integer getCurrentPage();

	/**
	 * Gets the volume number (one based) of the context described in the current context,
	 * or null if not available.
	 * 
	 * For example, an entry in a table of contents or an end note are examples
	 * where this method can be used to retrieve the original context.
	 * 
	 * @return returns the meta volume number
	 */
	public Integer getMetaVolume();

	/**
	 * Gets the page number (one based) of the context described the current context,
	 * or null if not available.
	 *  
	 * For example, an entry in a table of contents or an end note are examples
	 * where this method can be used to retrieve the original context.
	 * 
	 * 
	 * @return returns the meta page number
	 */
	public Integer getMetaPage();
}
