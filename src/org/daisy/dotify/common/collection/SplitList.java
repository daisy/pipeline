package org.daisy.dotify.common.collection;

import java.util.List;

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
	 * Splits the list into two parts at the specified position.
	 * @param in the original list
	 * @param pos the position to split (must be >=0 && <=size())
	 * @return returns the splitted list
	 */
	public static <T> SplitList<T> split(List<T> in, int pos) {
		return new SplitList<T>(in.subList(0, pos), in.subList(pos, in.size()));
	}

}
