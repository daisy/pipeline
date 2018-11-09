package org.daisy.dotify.formatter.impl.page;


/**
 * Provides an orphan/widow control utility
 * 
 * @author Joel Håkansson
 */
class OrphanWidowControl {
	private final int orphans, widows, size;

	/**
	 * Creates a new instance
	 * @param orphans the minimum number of paragraph-opening lines that may appear by themselves at the bottom of a page.
	 * @param widows the minimum number of paragraph-ending lines that may fall at the beginning of the following page.
	 * @param size the number of lines in the paragraph
	 */
	OrphanWidowControl(int orphans, int widows, int size) {
		this.orphans = orphans;
		this.widows = widows;
		this.size = size;
	}

	/**
	 * Returns true if break is allowed after the specified index, zero based.  
	 * @param index
	 * @return
	 * @throws IndexOutOfBounds if index is >= size or < 0
	 */
	boolean allowsBreakAfter(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (index>=size-1) { // the last index always allows breaking after
			return true;
		} else {
			return (index>=(orphans-1) && widows<(size-index));
		}
	}
}
