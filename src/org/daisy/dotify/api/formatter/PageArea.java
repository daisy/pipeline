package org.daisy.dotify.api.formatter;

/**
 * Specifies an area of the page where collection items can be placed. The
 * page area can be located before the text body, below the header or after
 * the text body, above the footer.
 * 
 * @author Joel Håkansson
 */
public interface PageArea {
	/**
	 * Specifies the alignment of the page area.
	 */
	public enum Alignment {
		/**
		 * Aligns toward the top of the page, below the header
		 */
		TOP,
		/**
		 * Aligns toward the bottom of the page, above the footer
		 */
		BOTTOM};
	
	/**
	 * Specifies the scope of the fallback action. 
	 *
	 */
	public enum FallbackScope {
		/**
		 * Specifies that all items in the collection should be reassigned,
		 * if at least one item cannot be rendered in its designated page area.
		 */
		ALL,
		/**
		 * Specified that items on the same page in the collection should be
		 * reassigned, if at least one item on the page cannot be rendered in
		 * the page area.
		 */
		PAGE};
	
	/**
	 * Gets the alignment of the page area.
	 * @return returns the alignment
	 */
	public Alignment getAlignment();
	
	/**
	 * Gets the id of the associated collection.
	 * @return returns the id of the associated collection
	 */
	public String getCollectionId();
	
	/**
	 * Gets the id of a fallback collection. Note that the
	 * collection id need not refer to an existing collection.
	 * 
	 * @return returns the id of the fallback collection
	 */
	public String getFallbackId();
	
	/**
	 * Gets the scope of the fallback action.
	 * @return returns the scope of the fallback action
	 */
	public FallbackScope getFallbackScope();
	
	/**
	 * Gets the maximum height allowed to be used by the page
	 * area, in rows.
	 * @return returns the maximum height of the page area in rows
	 */
	public int getMaxHeight();
}
