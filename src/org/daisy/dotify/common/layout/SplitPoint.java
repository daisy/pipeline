package org.daisy.dotify.common.layout;

import java.util.Collections;
import java.util.List;

/**
 * Provides a data object to keep the information about a split point result. 
 * @author Joel Håkansson
 */
public class SplitPoint<T extends SplitPointUnit> {

	private final List<T> head;
	private final List<T> supplements;
	private final List<T> tail;
	private final List<T> discarded;
	private final boolean hardBreak;

	/**
	 * Create a new SplitPoint.
	 * @param head the part of the original SplitPointUnit list that fits within the target breakpoint 
	 * @param supplements a list of supplement units
	 * @param tail the part of the original SplitPointUnit list that is left
	 * @param discarded a list of discarded units
	 * @param hardBreak set to true if a break point could not be achieved with respect for break point boundaries 
	 */
	public SplitPoint(List<T> head, List<T> supplements, List<T> tail, List<T> discarded, boolean hardBreak) {
		if (head == null) {
			head = Collections.emptyList();
		}
		if (supplements == null) {
			supplements = Collections.emptyList();
		}
		if (tail == null) {
			tail = Collections.emptyList();
		}
		if (discarded == null) {
			discarded = Collections.emptyList();
		}
		this.head = head;
		this.supplements = supplements;
		this.tail = tail;
		this.discarded = discarded;
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
	 * Gets the supplements.
	 * @return returns the supplements
	 */
	public List<T> getSupplements() {
		return supplements;
	}

	/**
	 * Get the tail part of the SplitPointUnit list
	 * @return returns the tail part of the SplitPointUnit list
	 */
	public List<T> getTail() {
		return tail;
	}
	
	/**
	 * Gets discarded units
	 * @return returns the discarded units, if any
	 */
	public List<T> getDiscarded() {
		return discarded;
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
		result = prime * result + ((discarded == null) ? 0 : discarded.hashCode());
		result = prime * result + (hardBreak ? 1231 : 1237);
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		result = prime * result + ((supplements == null) ? 0 : supplements.hashCode());
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
		if (discarded == null) {
			if (other.discarded != null) {
				return false;
			}
		} else if (!discarded.equals(other.discarded)) {
			return false;
		}
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
		if (supplements == null) {
			if (other.supplements != null) {
				return false;
			}
		} else if (!supplements.equals(other.supplements)) {
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
