package org.daisy.dotify.api.formatter;



/**
 * Provides an interface for a sequence of pages.
 * 
 * @author Joel Håkansson
 */
public interface PageSequence{
	
	public Iterable<? extends Page> getPages();
	/**
	 * Gets the number of pages in this sequence
	 * @return returns the number of pages in this sequence
	 */
	public int getPageCount();
	/**
	 * Gets the layout master for this sequence
	 * @return returns the layout master for this sequence
	 */
	public LayoutMaster getLayoutMaster();

}