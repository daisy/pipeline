package org.daisy.dotify.api.formatter;





/**
 * Specifies the layout of a paged media.
 * @author Joel Håkansson
 */
public interface LayoutMaster extends SectionProperties {

	/**
	 * Gets the template for the specified page number
	 * @param pagenum the page number to get the template for
	 * @return returns the template
	 */
	public PageTemplate getTemplate(int pagenum);

	/**
	 * Gets the page area for all pages using this master.
	 * @return returns the PageArea, or null if no page area is used.
	 */
	public PageAreaProperties getPageArea();

}
