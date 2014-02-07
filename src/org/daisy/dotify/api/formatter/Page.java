package org.daisy.dotify.api.formatter;

import java.util.List;

/**
 * Provides a page object.
 * 
 * @author Joel Håkansson
 */
public interface Page {
	
	/**
	 * Gets all markers for this page
	 * @return returns a list of all markers on a page
	 */
	//public List<Marker> getMarkers();
	
	/**
	 * Gets markers for this page excluding markers before text content
	 * @return returns a list of markers on a page
	 */
	//public List<Marker> getContentMarkers();
	
	/**
	 * Gets the rows on this page
	 * @return returns the rows on this page
	 */
	public List<Row> getRows();

	/**
	 * Gets the page index for this page (zero based, but including page number offset)
	 * @return returns the page index in the sequence
	 */
	public int getPageIndex();
	
	/**
	 * Returns true if this page allows volume break after it.
	 * @return returns true if the page allows volume break, false otherwise
	 */
	public boolean allowsVolumeBreak();
	
	public int keepPreviousSheets();

	/**
	 * Gets the parent sequence for this page
	 * @return returns the parent sequence
	 */
	public PageSequence getParent();

}