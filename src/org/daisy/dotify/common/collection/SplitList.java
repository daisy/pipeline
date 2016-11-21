package org.daisy.dotify.common.collection;

import java.util.List;

/**
 * Provides a way to split a list into two parts around a given 
 * position.
 * 
 * @author Joel Håkansson
 *
 * @param <T> the type of objects in the lists
 */
public class SplitList<T> {
	private final List<T> first, second;

	public SplitList(List<T> first, List<T> second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Gets the first part of the original list.
	 * @return returns the first part
	 */
	public List<T> getFirstPart() {
		return first;
	}

	/**
	 * Gets the second part of the original list.
	 * @return returns the second part
	 */
	public List<T> getSecondPart() {
		return second;
	}
	
	/**
	 * Splits the list into two parts at the specified position. The returned
	 * object is backed by the original list.
	 * 
	 * @param in the original list
	 * @param pos the position to split (must be &gt;=0 &amp;&amp; &lt;=size())
	 * @return returns the split list
	 */
	public static <T> SplitList<T> split(List<T> in, int pos) {
		return new SplitList<>(in.subList(0, pos), in.subList(pos, in.size()));
	}

}
