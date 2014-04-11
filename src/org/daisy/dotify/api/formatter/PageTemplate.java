package org.daisy.dotify.api.formatter;

import java.util.List;

/**
 * Specifies page objects such as header and footer
 * for the pages to which it applies.
 * @author Joel Håkansson
 */
public interface PageTemplate {

	/**
	 * Gets header rows for a page using this Template. Each FieldList must 
	 * fit within a single row, i.e. the combined length of all resolved strings in each FieldList must
	 * be smaller than the flow width. Keep in mind that text filters will be applied to the 
	 * resolved string, which could affect its length.
	 * @return returns a List of FieldList
	 */
	public List<FieldList> getHeader();
	
	/**
	 * Gets footer rows for a page using this Template. Each FieldList must 
	 * fit within a single row, i.e. the combined length of all resolved strings in each FieldList must
	 * be smaller than the flow width. Keep in mind that text filters will be applied to the 
	 * resolved string, which could affect its length.
	 * @return returns a List of FieldList
	 */
	public List<FieldList> getFooter();
	
	/**
	 * Tests if this Template applies to this pagenum.
	 * @param pagenum the pagenum to test
	 * @return returns true if the Template should be applied to the page
	 */
	public boolean appliesTo(int pagenum);

}