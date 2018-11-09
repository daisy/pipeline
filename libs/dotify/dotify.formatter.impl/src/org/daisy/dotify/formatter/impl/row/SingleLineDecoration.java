package org.daisy.dotify.formatter.impl.row;

/**
 * Provides a definition for a single line decoration.
 * {@link SingleLineDecoration}s are immutable. 
 * @author Joel Håkansson
 *
 */
public final class SingleLineDecoration {
	private final String leftCorner;
	private final String rightCorner;
	private final String linePattern;

	public SingleLineDecoration(String leftCorner,
			String linePattern, String rightCorner) {
		super();
		this.leftCorner = leftCorner;
		this.linePattern = linePattern;
		this.rightCorner = rightCorner;
	}

	public String getLeftCorner() {
		return leftCorner;
	}

	public String getRightCorner() {
		return rightCorner;
	}

	public String getLinePattern() {
		return linePattern;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftCorner == null) ? 0 : leftCorner.hashCode());
		result = prime * result + ((linePattern == null) ? 0 : linePattern.hashCode());
		result = prime * result + ((rightCorner == null) ? 0 : rightCorner.hashCode());
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
		SingleLineDecoration other = (SingleLineDecoration) obj;
		if (leftCorner == null) {
			if (other.leftCorner != null) {
				return false;
			}
		} else if (!leftCorner.equals(other.leftCorner)) {
			return false;
		}
		if (linePattern == null) {
			if (other.linePattern != null) {
				return false;
			}
		} else if (!linePattern.equals(other.linePattern)) {
			return false;
		}
		if (rightCorner == null) {
			if (other.rightCorner != null) {
				return false;
			}
		} else if (!rightCorner.equals(other.rightCorner)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SingleLineDecoration [leftCorner=" + leftCorner + ", rightCorner=" + rightCorner + ", linePattern="
				+ linePattern + "]";
	}

}
