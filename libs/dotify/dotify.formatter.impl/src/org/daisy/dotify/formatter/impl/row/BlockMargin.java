package org.daisy.dotify.formatter.impl.row;

/**
 * Provides the required margins. Immutable.
 * @author Joel Håkansson
 *
 */
class BlockMargin {
	private final MarginProperties leftParent;
	private final MarginProperties rightParent;
	private final MarginProperties leftMargin;
	private final MarginProperties rightMargin;

	BlockMargin(RowDataProperties rdp, char spaceCharacter) {
		this.leftParent = rdp.getLeftMargin().buildMarginParent(spaceCharacter);
		this.rightParent = rdp.getRightMargin().buildMarginParent(spaceCharacter);
		this.leftMargin = rdp.getLeftMargin().buildMargin(spaceCharacter);
		this.rightMargin = rdp.getRightMargin().buildMargin(spaceCharacter);	
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

}
