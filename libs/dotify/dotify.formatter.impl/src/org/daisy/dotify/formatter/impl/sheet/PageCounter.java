package org.daisy.dotify.formatter.impl.sheet;

/**
 * Provides state needed for a text flow.
 * 
 * @author Joel Håkansson
 */
public class PageCounter {
	private int pageOffset;
	private int pageCount;

	public PageCounter() {
		pageOffset = 0;
		pageCount = 0;
	}

	public PageCounter(PageCounter template) {
		this.pageOffset = template.pageOffset;
		this.pageCount = template.pageCount;
	}

	public void setDefaultPageOffset(int value) {
		pageOffset = value;
	}

	public int getDefaultPageOffset() {
		return pageOffset;
	}

	/**
	 * This is used for searching and MUST be continuous. Do not use for page numbers.
	 * @return returns the page count
	 */
	public int getPageCount() {
		return pageCount;
	}
	
	/**
	 * Advance to the next page.
	 */
	public void increasePageCount() {
		pageCount++;
	}

}