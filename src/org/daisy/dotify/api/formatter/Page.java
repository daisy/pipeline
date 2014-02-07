package org.daisy.dotify.api.formatter;

import java.util.List;

/**
 * Provides a page object.
 * 
 * @author Joel Håkansson
 */
public interface Page {

	/**
	 * Gets the rows on this page
	 * @return returns the rows on this page
	 */
	public List<Row> getRows();


}