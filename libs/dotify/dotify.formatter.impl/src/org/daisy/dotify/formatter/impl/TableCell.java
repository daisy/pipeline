package org.daisy.dotify.formatter.impl;


import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.TableCellProperties;

class TableCell extends FormatterCoreImpl {
	/**
	 * 
	 */
	private static final long serialVersionUID = -673589204065659433L;
	private final TableCellInfo info;
	private CellData rendered;


	TableCell(FormatterCoreContext fc, TableCellProperties props, GridPoint p) {
		this(fc, props, false, p);
	}
	
	TableCell(FormatterCoreContext fc, TableCellProperties props, boolean discardIdentifiers, GridPoint p) {
		super(fc, discardIdentifiers);
		this.info = new TableCellInfo(props, p);
		this.rendered = null;
	}

	TableCellInfo getInfo() {
		return info;
	}
	
	CellData render(FormatterContext context, DefaultContext c, CrossReferenceHandler crh, int flowWidth) {
		List<RowImpl> rowData = new ArrayList<>();
		List<Block> blocks = getBlocks(context, c, crh);
		int minWidth = flowWidth;
		int forceCount = 0;
		boolean isVolatile = false;
		for (Block block : blocks) {
			AbstractBlockContentManager bcm = block.getBlockContentManager(
					new BlockContext(flowWidth, crh, c, context)
					);
			isVolatile |= bcm.isVolatile();
			forceCount += bcm.getForceBreakCount();
			minWidth = Math.min(bcm.getMinimumAvailableWidth(), minWidth);
			rowData.addAll(bcm.getCollapsiblePreContentRows());
			rowData.addAll(bcm.getInnerPreContentRows());
			for (RowImpl r2 : bcm) {
				rowData.add(r2);
			}
			rowData.addAll(bcm.getPostContentRows());
			rowData.addAll(bcm.getSkippablePostContentRows());
		}
		rendered = new CellData(rowData, flowWidth, info, minWidth, forceCount, isVolatile);
		return rendered;
	}

	CellData getRendered() {
		return rendered;
	}

}
