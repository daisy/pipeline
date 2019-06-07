package org.daisy.dotify.formatter.impl.row;

/**
 * Provides the required margins. Immutable.
 * @author Joel Håkansson
 *
 */
public class BlockMargin {
	private final MarginProperties leftParent;
	private final MarginProperties rightParent;
	private final MarginProperties leftMargin;
	private final MarginProperties rightMargin;

	public BlockMargin(Margin left, Margin right, char spaceCharacter) {
		this.leftParent = left.buildMarginParent(spaceCharacter);
		this.rightParent = right.buildMarginParent(spaceCharacter);
		this.leftMargin = left.buildMargin(spaceCharacter);
		this.rightMargin = right.buildMargin(spaceCharacter);	
	}

	public MarginProperties getLeftParent() {
		return leftParent;
	}

	public MarginProperties getRightParent() {
		return rightParent;
	}

	public MarginProperties getLeftMargin() {
		return leftMargin;
	}

	public MarginProperties getRightMargin() {
		return rightMargin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftMargin == null) ? 0 : leftMargin.hashCode());
		result = prime * result + ((leftParent == null) ? 0 : leftParent.hashCode());
		result = prime * result + ((rightMargin == null) ? 0 : rightMargin.hashCode());
		result = prime * result + ((rightParent == null) ? 0 : rightParent.hashCode());
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
		BlockMargin other = (BlockMargin) obj;
		if (leftMargin == null) {
			if (other.leftMargin != null) {
				return false;
			}
		} else if (!leftMargin.equals(other.leftMargin)) {
			return false;
		}
		if (leftParent == null) {
			if (other.leftParent != null) {
				return false;
			}
		} else if (!leftParent.equals(other.leftParent)) {
			return false;
		}
		if (rightMargin == null) {
			if (other.rightMargin != null) {
				return false;
			}
		} else if (!rightMargin.equals(other.rightMargin)) {
			return false;
		}
		if (rightParent == null) {
			if (other.rightParent != null) {
				return false;
			}
		} else if (!rightParent.equals(other.rightParent)) {
			return false;
		}
		return true;
	}

}
