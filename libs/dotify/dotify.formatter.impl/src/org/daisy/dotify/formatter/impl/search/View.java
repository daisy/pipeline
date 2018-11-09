package org.daisy.dotify.formatter.impl.search;

import java.util.List;

public class View<T> {
	private final int fromIndex;
	protected final List<T> items;
	private int toIndex;

	protected View(List<T> items, int fromIndex) {
		this(items, fromIndex, fromIndex);
	}
	
	protected View(List<T> items, int fromIndex, int toIndex) {
		this.items = items;
		this.fromIndex = fromIndex;
		this.setToIndex(toIndex);
	}
	
	/**
	 * Gets the number of items in this sequence
	 * @return returns the number of items in this sequence
	 */
	public int size() {
		return getToIndex()-fromIndex;
	}

	public T get(int index) {
		if (index<0 || index>=size()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return items.get(index+fromIndex);
	}

	public List<T> getItems() {
		return items.subList(fromIndex, getToIndex());
	}
	int toLocalIndex(int globalIndex) {
		return globalIndex-fromIndex;
	}
	
	/**
	 * Gets the index for the first item in this sequence, counting all preceding items in the document, zero-based. 
	 * @return returns the first index
	 */
	public int getGlobalStartIndex() {
		return fromIndex;
	}

	public int getToIndex() {
		return toIndex;
	}

	public void setToIndex(int toIndex) {
		this.toIndex = toIndex;
	}

}
