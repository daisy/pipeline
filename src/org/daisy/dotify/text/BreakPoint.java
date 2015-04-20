package org.daisy.dotify.text;

/**
 * <p>A BreakPoint is a data object to keep the information about a break point result.
 * Since this implementation uses two Strings, rather than the original String and
 * an integer for the break point position, it can be used with non standard hyphenation 
 * algorithms.</p>
 * @author Joel Håkansson, TPB
 */
public class BreakPoint {
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hardBreak ? 1231 : 1237);
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		result = prime * result + ((tail == null) ? 0 : tail.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BreakPoint other = (BreakPoint) obj;
		if (hardBreak != other.hardBreak)
			return false;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (!head.equals(other.head))
			return false;
		if (tail == null) {
			if (other.tail != null)
				return false;
		} else if (!tail.equals(other.tail))
			return false;
		return true;
	}

	private final String head;
	private final String tail;
	private final boolean hardBreak;

	/**
	 * Create a new BreakPoint.
	 * @param head the part of the original String that fits within the target break point 
	 * @param tail the part of the original String that is left
	 * @param hardBreak set to true if a break point could not be achieved with respect for break point boundaries 
	 */
	public BreakPoint(String head, String tail, boolean hardBreak) {
		this.head = head;
		this.tail = tail;
		this.hardBreak = hardBreak;
	}
	
	/**
	 * Get the head part of the BreakPoint String
	 * @return returns the head part of the BreakPoint String
	 */
	public String getHead() {
		return head;
	}

	/**
	 * Get the tail part of the BreakPoint String
	 * @return returns the tail part of the BreakPoint String
	 */
	public String getTail() {
		return tail;
	}
	
	/**
	 * Test if this BreakPoint was achieved by breaking on a character other 
	 * than a valid break point character (typically hyphen, soft hyphen or space).
	 * @return returns true if this BreakPoint was achieved by breaking on a character other than hyphen, soft hyphen or space
	 */
	public boolean isHardBreak() {
		return hardBreak;
	}
}
