package org.daisy.dotify.api.formatter;

/**
 * Provides methods needed to add TOC entries to a formatter.
 * 
 * @author Joel Håkansson
 */
public interface TocEntry {
	
	/**
	 * Creates a new entry and returns a formatter that can be used to add
	 * contents to this entry.
	 * 
	 * @param refId the element that this toc entry is connected to
	 * @param props the properties
	 * @return returns a formatter that can be used to add contents
	 */
	public FormatterToc newEntry(String refId, BlockProperties props);

}
