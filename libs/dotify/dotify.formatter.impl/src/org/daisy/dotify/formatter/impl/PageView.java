package org.daisy.dotify.formatter.impl;

import java.util.List;

class PageView {
	private final int fromIndex;
	protected final List<PageImpl> pages;
	protected int toIndex;

	PageView(List<PageImpl> pages, int fromIndex) {
		this(pages, fromIndex, fromIndex);
	}
	
	PageView(List<PageImpl> pages, int fromIndex, int toIndex) {
		this.pages = pages;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}
	
	/**
	 * Gets the number of pages in this sequence
	 * @return returns the number of pages in this sequence
	 */
	public int getPageCount() {
		return toIndex-fromIndex;
	}

	public PageImpl getPage(int index) {
		if (index<0 || index>=getPageCount()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return pages.get(index+fromIndex);
	}

	public List<PageImpl> getPages() {
		return pages.subList(fromIndex, toIndex);
	}

	boolean isSequenceEmpty() {
		return toIndex-fromIndex == 0;
	}
	
	PageImpl peek() {
		return pages.get(toIndex-1);
	}
	
	int toLocalIndex(int globalIndex) {
		return globalIndex-fromIndex;
	}
	
	/**
	 * Gets the index for the first page in this sequence, counting all preceding pages in the document, zero-based. 
	 * @return returns the first index
	 */
	int getGlobalStartIndex() {
		return fromIndex;
	}

}
