package org.daisy.dotify.api.formatter;

/**
 * Provides a page template builder.
 * @author Joel Håkansson
 *
 */
public interface PageTemplateBuilder {

	/**
	 * Adds a line to the header
	 * @param obj the field list
	 */
	public void addToHeader(FieldList obj);

	/**
	 * Adds a line to the header
	 * @param obj the field list
	 */
	public void addToFooter(FieldList obj);
	
	/**
	 * Adds a column to the left margin of the page.
	 * @param margin the column
	 */
	public void addToLeftMargin(MarginRegion margin);
	
	/**
	 * Adds a column to the right margin of the page.
	 * @param margin the column
	 */
	public void addToRightMargin(MarginRegion margin);
}
