package org.daisy.dotify.formatter.impl.search;

public final class Overhead {
	private final int preContentSize;
	private final int postContentSize;
	
	/**
	 * @param preContentSize
	 * @param postContentSize
	 */
	Overhead(int preContentSize, int postContentSize) {
		super();
		this.preContentSize = preContentSize;
		this.postContentSize = postContentSize;
	}
	
	public Overhead withPreContentSize(int s) {
		return new Overhead(s, getPostContentSize());
	}
	
	public Overhead withPostContentSize(int s) {
		return new Overhead(getPreContentSize(), s);
	}

	public int getPreContentSize() {
		return preContentSize;
	}
	public int getPostContentSize() {
		return postContentSize;
	}
	
	public int total() {
		return preContentSize + postContentSize;
	}
	

}
