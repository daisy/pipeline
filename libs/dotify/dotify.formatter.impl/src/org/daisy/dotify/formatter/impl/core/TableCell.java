package org.daisy.dotify.formatter.impl.core;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

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
		for (Block block : blocks) {
			AbstractBlockContentManager bcm = block.getBlockContentManager(
					BlockContext.from(c).flowWidth(flowWidth).formatterContext(context).build()
					);
			rowData.addAll(bcm.getCollapsiblePreContentRows());
			rowData.addAll(bcm.getInnerPreContentRows());
			Optional<RowImpl> r;
			while ((r=bcm.getNext()).isPresent()) {
				rowData.add(r.get());
			}
			forceCount += bcm.getForceBreakCount();
			minWidth = Math.min(bcm.getMinimumAvailableWidth(), minWidth);
			rowData.addAll(bcm.getPostContentRows());
			rowData.addAll(bcm.getSkippablePostContentRows());
		}
		rendered = new CellData(rowData, flowWidth, info, minWidth, forceCount);
		return rendered;
	}

	CellData getRendered() {
		return rendered;
	}

}
