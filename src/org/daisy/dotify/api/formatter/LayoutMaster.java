package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.translator.TextBorderStyle;





/**
 * Specifies the layout of a paged media.
 * @author Joel Håkansson
 */
public interface LayoutMaster {

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
	
	/**
	 * Gets the page width.
	 * An implementation must ensure that getPageWidth()=getFlowWidth()+getInnerMargin()+getOuterMargin()
	 * @return returns the page width
	 */
	public int getPageWidth();

	/**
	 * Gets the page height.
	 * An implementation must ensure that getPageHeight()=getHeaderHeight()+getFlowHeight()+getFooterHeight()
	 * @return returns the page height
	 */
	public int getPageHeight();

	/**
	 * Gets row spacing, in row heights. For example, use 2.0 for double row spacing and 1.0 for normal row spacing.
	 * @return returns row spacing
	 */
	public float getRowSpacing();
	
	/**
	 * Returns true if output is intended on both sides of the sheets
	 * @return returns true if output is intended on both sides of the sheets
	 */
	public boolean duplex();

	/**
	 * Gets the flow width
	 * @return returns the flow width
	 */
	public int getFlowWidth();

	/**
	 * Gets the border.
	 * @return the border
	 */
	public TextBorderStyle getBorder();

	/**
	 * Gets inner margin
	 * @return returns the inner margin
	 */
	public int getInnerMargin();

	/**
	 * Gets outer margin
	 * @return returns the outer margin
	 */
	public int getOuterMargin();
	

}
