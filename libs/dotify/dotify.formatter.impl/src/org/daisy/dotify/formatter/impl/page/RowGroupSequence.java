package org.daisy.dotify.formatter.impl.page;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.formatter.impl.core.Block;

/**
 * Sequence of {@link Block}s starting at a hard page break (<code>break-before="page"</code> or
 * <code>break-before="sheet"</code>) or a block with absolute positioning
 * (<code>vertical-position</code>).
 */
class RowGroupSequence {
	private final List<Block> blocks;
	private final BreakBefore breakBefore;
	private VerticalSpacing vSpacing;
	private List<RowGroup> group;

	public RowGroupSequence(BreakBefore breakBefore, VerticalSpacing vSpacing) {
		this(breakBefore, vSpacing, new ArrayList<>(), new ArrayList<>());
	}
	
	public RowGroupSequence(BreakBefore breakBefore, VerticalSpacing vSpacing, List<Block> blocks, List<RowGroup> group) {
		this.blocks = blocks;
		this.group = group;
		this.vSpacing = vSpacing;
		this.breakBefore = breakBefore;
	}
	
	/**
	 * Creates a deep copy of the supplied instance
	 * @param template the instance to copy
	 */
	RowGroupSequence(RowGroupSequence template) {
		this(template, template.vSpacing, 0, true);
	}

	RowGroupSequence(RowGroupSequence template, int offset) {
		this(template, template.vSpacing, offset, false);
	}
	
	private RowGroupSequence(RowGroupSequence template, VerticalSpacing vs, int offset, boolean deepMode) {
		this.blocks = deepMode?new ArrayList<>(template.blocks):template.blocks;
		if (deepMode) {
			this.group = new ArrayList<>();
			for (RowGroup rg : template.group) {
				group.add(new RowGroup(rg));
			}
		} else {
			if (template.group==null) {
				this.group = null;
			} else if (template.group.size()>offset) {
				this.group = new ArrayList<>(
						offset>0?template.group.subList(offset, template.group.size()):template.group);
			} else {
				this.group = new ArrayList<>();
			}
		}
		this.vSpacing = vs;
		this.breakBefore = template.breakBefore;
	}

	List<RowGroup> getGroup() {
		return group;
	}
	
	void setGroup(List<RowGroup> value) {
		this.group = value;
	}
	
	List<Block> getBlocks() {
		return blocks;
	}
	
	VerticalSpacing getVerticalSpacing() {
		return vSpacing;
	}
	
	void setVerticalSpacing(VerticalSpacing vs) {
		this.vSpacing = vs;
	}

	BreakBefore getBreakBefore() {
		return breakBefore;
	}
}
