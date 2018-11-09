package org.daisy.dotify.formatter.impl.page;

import java.util.Objects;

import org.daisy.dotify.api.formatter.BlockPosition;
import org.daisy.dotify.formatter.impl.row.RowImpl;

class VerticalSpacing {
	private final BlockPosition pos;
	private final RowImpl emptyRow;
	
	VerticalSpacing(BlockPosition pos, RowImpl emptyRow) {
		Objects.requireNonNull(pos);
		Objects.requireNonNull(emptyRow);
		this.pos = pos;
		this.emptyRow = emptyRow;
	}

	public BlockPosition getBlockPosition() {
		return pos;
	}

	public RowImpl getEmptyRow() {
		return emptyRow;
	}

}
