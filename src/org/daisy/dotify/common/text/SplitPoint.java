package org.daisy.dotify.common.text;

import java.util.Collections;
import java.util.List;

/**
 * A SplitPoint is a data object to keep the information about a split point result.
 * 
 * @author Joel Håkansson
 */
public class SplitPoint<T extends SplitPointUnit> {

	private final List<T> head;
	private final List<T> tail;
	private final boolean hardBreak;

	/**
	 * Create a new SplitPoint.
	 * @param head the part of the original SplitPointUnit list that fits within the target breakpoint 
	 * @param tail the part of the original SplitPointUnit list that is left
	 * @param hardBreak set to true if a break point could not be achieved with respect for break point boundaries 
	 */
	public SplitPoint(List<T> head, List<T> tail, boolean hardBreak) {
		if (head == null) {
			head = Collections.emptyList();
		}
		if (tail == null) {
			tail = Collections.emptyList();
		}
		this.head = head;
		this.tail = tail;
		this.hardBreak = hardBreak;
	}
	
	/**
	 * Get the head part of the SplitPointUnit list
	 * @return returns the head part of the SplitPointUnit list
	 */
	public List<T> getHead() {
		return head;
	}

	/**
	 * Get the tail part of the SplitPointUnit list
	 * @return returns the tail part of the SplitPointUnit list
	 */
	public List<T> getTail() {
		return tail;
	}
	
	/**
	 * Test if this SplitPoint was achieved by breaking on a unit other 
	 * than a breakpoint.
	 * @return returns true if this SplitPoint was achieved by breaking on a unit other than a breakpoint
	 */
	public boolean isHardBreak() {
		return hardBreak;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hardBreak ? 1231 : 1237);
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		result = prime * result + ((tail == null) ? 0 : tail.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SplitPoint<?> other = (SplitPoint<?>) obj;
		if (hardBreak != other.hardBreak) {
			return false;
		}
		if (head == null) {
			if (other.head != null) {
				return false;
			}
		} else if (!head.equals(other.head)) {
			return false;
		}
		if (tail == null) {
			if (other.tail != null) {
				return false;
			}
		} else if (!tail.equals(other.tail)) {
			return false;
		}
		return true;
	}
}
