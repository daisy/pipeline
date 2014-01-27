package org.daisy.dotify.api.formatter;

/**
 * Provides methods needed to add a TOC to a formatter. Note
 * that adding contents outside of entries has no specified
 * meaning and may be ignored by a formatter. 
 * 
 * @author Joel Håkansson
 */
public interface TableOfContents extends FormatterCore {

	/**
	 * Starts a new entry with the supplied properties.
	 * 
	 * @param refId the element that this toc entry is connected to
	 * @param props the properties
	 */
	public void startEntry(String refId, BlockProperties props);
	
	/**
	 * Ends the current entry.
	 */
	public void endEntry();
	
	/**
	 * Inserts an expression to evaluate.
	 * @param exp the expression
	 * @param t the text properties
	 */
	public void insertEvaluate(String exp, TextProperties t);
	
}
