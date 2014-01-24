package org.daisy.dotify.api.formatter;

/**
 * Provides a formatted result, in various facets.
 * 
 * @author Joel Håkansson
 */
public interface Formatted {

	/**
	 * Gets the rows.
	 * 
	 * @return returns the rows
	 */
	public Iterable<String> getRows();

	/**
	 * Gets the rows.
	 * 
	 * @param page
	 *            , the page for the rows, one based
	 * @return returns the rows
	 */
	public Iterable<String> getRows(int page);

	/**
	 * Gets the rows.
	 * 
	 * @param page
	 *            , the page for the rows, one based
	 * @param volume
	 *            , the volume for the rows, one based
	 * @return returns the rows
	 */
	public Iterable<String> getRows(int page, int volume);

	/**
	 * Gets the pages, or null if the result is not formatted in pages.
	 * 
	 * @return returns the pages
	 */
	public Iterable<Page> getPages();

	/**
	 * Gets the pages, or null if the result is not formatted in pages.
	 * 
	 * @param volume
	 *            , the volume for the pages, one based
	 * @return returns the pages
	 */
	public Iterable<Page> getPages(int volume);

	/**
	 * Gets the volumes, or null if the result is not formatted in volumes.
	 * 
	 * @return returns the volumes
	 */
	public Iterable<Volume> getVolumes();

}
