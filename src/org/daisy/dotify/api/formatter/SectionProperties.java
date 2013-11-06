package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.translator.TextBorderStyle;


/**
 * Specifies the configuration properties for a section of pages
 * in a paged media.
 * @author Joel Håkansson
 */
public interface SectionProperties {

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

	public TextBorderStyle getFrame();

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
