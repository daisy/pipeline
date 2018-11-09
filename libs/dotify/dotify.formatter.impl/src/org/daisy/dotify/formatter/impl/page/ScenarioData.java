package org.daisy.dotify.formatter.impl.page;

import java.util.List;
import java.util.Stack;

import org.daisy.dotify.api.formatter.FormattingTypes.BreakBefore;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.BlockContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;

/**
 * Provides data about a single rendering scenario.
 * 
 * @author Joel Håkansson
 */
class ScenarioData extends BlockProcessor {
	private Stack<RowGroupSequence> dataGroups = new Stack<>();

	ScenarioData() {
		super();
		dataGroups = new Stack<>();
	}

	/**
	 * Creates a deep copy of the supplied instance
	 * @param template the instance to copy
	 */
	ScenarioData(ScenarioData template) {
		super(template);
		dataGroups = new Stack<>();
		for (RowGroupSequence rgs : template.dataGroups) {
			dataGroups.add(new RowGroupSequence(rgs));
		}
	}

	float calcSize() {
		float size = 0;
		for (RowGroupSequence rgs : dataGroups) {
			for (RowGroup rg : rgs.getGroup()) {
				size += rg.getUnitSize();
			}
		}
		return size;
	}
	
	private boolean isDataEmpty() {
		return (dataGroups.isEmpty()||dataGroups.peek().getGroup().isEmpty());
	}
	
	protected boolean hasSequence() {
		return !dataGroups.isEmpty();
	}
	
	protected boolean hasResult() {
		return !isDataEmpty();
	}
	
	protected void newRowGroupSequence(BreakBefore breakBefore, VerticalSpacing vs) {
		RowGroupSequence rgs = new RowGroupSequence(breakBefore, vs);
		dataGroups.add(rgs);
	}
	
	protected void addRowGroup(RowGroup rg) {
		dataGroups.peek().getGroup().add(rg);
	}
	
	RowGroup peekResult() {
		return dataGroups.peek().currentGroup();
	}

	List<RowGroupSequence> getDataGroups() {
		return dataGroups;
	}
	
	void processBlock(LayoutMaster master, Block g, BlockContext bc) {
		loadBlock(master, g, bc);
		while (hasNextInBlock()) {
			processNextRowGroup(bc, false);
		}
		dataGroups.peek().getBlocks().add(g);
	}
}