package org.daisy.dotify.formatter.impl.core;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.api.formatter.TextBlockProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

class TableRow implements Iterable<TableCell> {
	private final Stack<TableCell> cells;
	private final FormatterCoreContext context;
	private int rowHeight, maxCellHeight;

	TableRow(FormatterCoreContext fc) {
		this.context = fc;
		rowHeight = 1; // empty rows have height 1
		maxCellHeight = 1;
		cells = new Stack<>();
	}
	
	TableCell beginsTableCell(TableCellProperties props, GridPoint p) {
		if (cells.isEmpty())  {
			// for the first cell we set the row height to the cell's rowspan, otherwise the value could never be larger than 1
			rowHeight = props.getRowSpan();
		} else {
			rowHeight = Math.min(rowHeight, props.getRowSpan());
		}
		maxCellHeight = Math.max(maxCellHeight, props.getRowSpan());
		TableCell fc = new TableCell(context, props, p);
		TextBlockProperties tbp = props.getTextBlockProperties();
		fc.startBlock(new BlockProperties.Builder()
				.bottomPadding(props.getPadding().getBottomSpacing())
				.topPadding(props.getPadding().getTopSpacing())
				.leftPadding(props.getPadding().getLeftSpacing())
				.rightPadding(props.getPadding().getRightSpacing())
				.align(tbp.getAlignment())
				.firstLineIndent(tbp.getFirstLineIndent())
				.textIndent(tbp.getTextIndent())
				.identifier(tbp.getIdentifier())
				.build());
		cells.push(fc);
		return fc;
	}
	
	/**
	 * 
	 * @return
	 * @throws IllegalStateException if the row is empty
	 */
	TableCell getCurrentCell() {
		try {
			return cells.peek();
		} catch (EmptyStackException e) {
			throw new IllegalStateException(e);
		}
	}
	
	void endsTableCell() {
		if (!cells.empty()) {
			cells.peek().endBlock();
		}
	}
	
	int cellCount() {
		return cells.size();
	}
	
	TableCell getCell(int index) {
		return cells.get(index);
	}
	
	int getRowHeight() {
		return rowHeight;
	}
	
	int getMaxCellHeight() {
		return maxCellHeight;
	}

	@Override
	public Iterator<TableCell> iterator() {
		return cells.iterator();
	}

}
