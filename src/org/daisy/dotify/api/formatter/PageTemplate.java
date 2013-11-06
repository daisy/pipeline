package org.daisy.dotify.api.formatter;

import java.util.List;

/**
 * Specifies page objects such as header and footer
 * for the pages to which it applies.
 * @author Joel Håkansson
 */
public interface PageTemplate {
	
	/**
	 * Get the header height.
	 * An implementation must ensure that getHeaderHeight()=getHeader().size() for all pagenum's
	 * @return returns the header height
	 */
	public int getHeaderHeight();

	/**
	 * Get the footer height.
	 * An implementation must ensure that getFooterHeight()=getFooter().size() for all pagenum's
	 * @return returns the footer height
	 */
	public int getFooterHeight();

	/**
	 * Get header rows for a page using this Template. Each ArrayList must 
	 * fit within a single row, i.e. the combined length of all resolved strings in each ArrayList must
	 * be smaller than the flow width. Keep in mind that text filters will be applied to the 
	 * resolved string, which could affect its length.
	 * @return returns an ArrayList containing an ArrayList of String
	 */
	public List<List<Field>> getHeader();
	
	/**
	 * Get footer rows for a page using this Template. Each ArrayList must 
	 * fit within a single row, i.e. the combined length of all resolved strings in each ArrayList must
	 * be smaller than the flow width. Keep in mind that text filters will be applied to the 
	 * resolved string, which could affect its length.
	 * @return returns an ArrayList containing an ArrayList of String
	 */
	public List<List<Field>> getFooter();
	
	/**
	 * Test if this Template applies to this pagenum.
	 * @param pagenum the pagenum to test
	 * @return returns true if the Template should be applied to the page
	 */
	public boolean appliesTo(int pagenum);

}