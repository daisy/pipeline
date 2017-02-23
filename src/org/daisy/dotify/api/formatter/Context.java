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
	public default Integer getCurrentVolume() {
		return null;
	}

	/**
	 * Gets the volume count or null if not known.
	 * @return returns the volume count
	 */
	public default Integer getVolumeCount() {
		return null;
	}

	/**
	 * Gets the current page number (one based) or null if not known.
	 * @return returns the current page number
	 */
	public default Integer getCurrentPage() {
		return null;
	}

	/**
	 * Gets the volume number (one based) of the context described in the current context,
	 * or null if not available.
	 * 
	 * For example, an entry in a table of contents or an end note are examples
	 * where this method can be used to retrieve the original context.
	 * 
	 * @return returns the meta volume number
	 */
	public default Integer getMetaVolume() {
		return null;
	}

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
	public default Integer getMetaPage() {
		return null;
	}
	
	/**
	 * Gets the total number of pages contained in the current volume, or null if not
	 * known. This should include the volume's pre- and post-content.
	 * @return returns the number of pages in the current volume, or null if not known.
	 */
	public default Integer getPagesInVolume() {
		return null;
	}
	
	/**
	 * Gets the total number of pages contained in the document, or null if not known.
	 * @return returns the number of pages in the document, or null if not known.
	 */
	public default Integer getPagesInDocument() {
		return null;
	}
	
	/**
	 * Gets the total number of sheets contained in the current volume, or null if not
	 * known. This should include the volume's pre- and post-content.
	 * @return returns the number of sheets in the current volume, or null if not known.
	 */
	public default Integer getSheetsInVolume() {
		return null;
	}
	
	/**
	 * Gets the total number of sheets contained in the document, or null if not known.
	 * @return returns the number of sheets in the document, or null if not known.
	 */
	public default Integer getSheetsInDocument() {
		return null;
	}
	
}
