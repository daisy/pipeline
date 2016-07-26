package org.daisy.dotify.formatter.impl;

import java.util.Iterator;
import java.util.List;

public class TableBlockContentManager extends AbstractBlockContentManager {
	private final List<RowImpl> rows;
	private final int forceCount;

	public TableBlockContentManager(int flowWidth, int minWidth, int forceCount, List<RowImpl> rows, RowDataProperties rdp, FormatterContext fcontext, boolean isVolatile) {
		super(flowWidth, rdp, fcontext);
		this.minWidth = minWidth;
		this.rows = rows;
		this.forceCount = forceCount;
		this.isVolatile = isVolatile;
	}
	
	@Override
	public Iterator<RowImpl> iterator() {
		return rows.iterator();
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	int getForceBreakCount() {
		return forceCount;
	}

}
