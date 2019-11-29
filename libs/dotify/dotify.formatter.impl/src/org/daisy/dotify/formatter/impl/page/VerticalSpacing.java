package org.daisy.dotify.formatter.impl.page;

import java.util.Objects;

import org.daisy.dotify.api.formatter.BlockPosition;
import org.daisy.dotify.formatter.impl.row.RowImpl;

/**
 * A wrapper around a {@link BlockPosition}, i.e. a combination of a <code>vertical-position</code>
 * and a <code>vertical-align</code>, to specify a vertical position of a block on a page.
 *
 * <p>{@link #getEmptyRow()} is for inserting padding before the block. The height (row spacing) of
 * the returned {@link RowImpl} should be (or is assumed to be) equal to the row spacing of the
 * current <code>layout-master</code> (which is also the unit for <code>vertical-position</code>).
 */
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
