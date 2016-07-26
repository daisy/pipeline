package org.daisy.dotify.formatter.impl;

import java.util.List;

public interface TableCost {

	public double getCost();
	
	public void addCell(List<RowImpl> rows, int cellWidth, int forceCount);
	
	public void completeTable(List<RowImpl> rows, int columnCount);
}
