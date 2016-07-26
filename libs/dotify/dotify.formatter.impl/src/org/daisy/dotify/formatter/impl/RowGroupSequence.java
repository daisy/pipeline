package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.BlockPosition;

class RowGroupSequence {
	private final List<RowGroup> group;
	private final BlockPosition pos;
	private final RowImpl emptyRow;

	public RowGroupSequence(BlockPosition pos, RowImpl emptyRow) {
		this.group = new ArrayList<RowGroup>();
		this.pos = pos;
		this.emptyRow = emptyRow;
	}
	
	/**
	 * Creates a deep copy of the supplied instance
	 * @param template the instance to copy
	 */
	RowGroupSequence(RowGroupSequence template) {
		this.group = new ArrayList<>();
		for (RowGroup rg : template.group) {
			group.add(new RowGroup(rg));
		}
		this.pos = template.pos;
		this.emptyRow = new RowImpl(template.emptyRow);
	}

	public List<RowGroup> getGroup() {
		return group;
	}
	
	RowGroup currentGroup() {
		if (group.isEmpty()) {
			return null;
		} else {
			return group.get(group.size()-1);
		}
	}

	public BlockPosition getBlockPosition() {
		return pos;
	}

	public RowImpl getEmptyRow() {
		return emptyRow;
	}
	
	float calcSequenceSize() {
		float ret = 0;
		for (RowGroup rg : getGroup()) {
			ret += rg.getUnitSize();
		}
		return ret;
	}

}
