package org.daisy.dotify.formatter.impl;

import java.util.List;

import org.daisy.dotify.api.writer.Row;

/**
 * Provides a page object.
 * 
 * @author Joel Håkansson
 */
interface Page {

	/**
	 * Gets the rows on this page
	 * @return returns the rows on this page
	 */
	public List<? extends Row> getRows();


}