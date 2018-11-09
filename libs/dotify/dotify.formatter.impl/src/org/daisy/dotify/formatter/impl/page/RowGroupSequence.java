package org.daisy.dotify.formatter.impl.page;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.formatter.impl.core.Block;

class RowGroupSequence {
	private final List<Block> blocks;
	private final List<RowGroup> group;
	private final VerticalSpacing vSpacing;
	private final BreakBefore breakBefore;

	public RowGroupSequence(BreakBefore breakBefore, VerticalSpacing vSpacing) {
		this.blocks = new ArrayList<>();
		this.group = new ArrayList<RowGroup>();
		this.vSpacing = vSpacing;
		this.breakBefore = breakBefore;
	}
	
	/**
	 * Creates a deep copy of the supplied instance
	 * @param template the instance to copy
	 */
	RowGroupSequence(RowGroupSequence template) {
		this.blocks = new ArrayList<>(template.blocks);
		this.group = new ArrayList<>();
		for (RowGroup rg : template.group) {
			group.add(new RowGroup(rg));
		}
		this.vSpacing = template.vSpacing;
		this.breakBefore = template.breakBefore;
	}

	@Deprecated
	public List<RowGroup> getGroup() {
		return group;
	}
	
	List<Block> getBlocks() {
		return blocks;
	}

	RowGroup currentGroup() {
		if (group.isEmpty()) {
			return null;
		} else {
			return group.get(group.size()-1);
		}
	}
	
    VerticalSpacing getVerticalSpacing() {
        return vSpacing;
    }

	BreakBefore getBreakBefore() {
		return breakBefore;
	}
}
