package org.daisy.dotify.formatter.impl;

import java.util.Iterator;
import java.util.List;

class CellData {
	private final List<RowImpl> rows;
	private final TableCellInfo info;
	private final int cellWidth;
	private final int minWidth;
	private final int forceCount;
	private final boolean isVolatile;
	private Iterator<RowImpl> rowsIterator;

	CellData(List<RowImpl> rows, int cellWidth, TableCellInfo info, int minWidth, int forceCount, boolean isVolatile) {
		this.rows = rows;
		this.info = info;
		this.cellWidth = cellWidth;
		this.minWidth = minWidth;
		this.forceCount = forceCount;
		this.rowsIterator = rows.iterator();
		this.isVolatile = isVolatile;
	}
	
	List<RowImpl> getRows() {
		return rows;
	}

	/**
	 * Gets a consumable row iterator
	 * @return
	 */
	Iterator<RowImpl> getRowIterator() {
		return rowsIterator;
	}
	
	void restartIterator() {
		rowsIterator = rows.iterator();
	}

	int getCellWidth() {
		return cellWidth;
	}

	int getMinWidth() {
		return minWidth;
	}

	TableCellInfo getInfo() {
		return info;
	}
	
	int getForceCount() {
		return forceCount;
	}
	
	boolean isVolatile() {
		return isVolatile;
	}
	
}