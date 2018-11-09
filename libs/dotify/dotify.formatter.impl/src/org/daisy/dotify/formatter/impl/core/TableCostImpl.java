package org.daisy.dotify.formatter.impl.core;

import java.util.List;

import org.daisy.dotify.formatter.impl.row.RowImpl;

class TableCostImpl implements TableCost {
	private final int spacePreferred;
	private double cost;

	TableCostImpl(int spacePreferred) {
		this.spacePreferred = spacePreferred;
		this.cost = 0;
	}

	@Override
	public double getCost() {
		return cost;
	}

	@Override
	public void addCell(List<RowImpl> rows, int cellWidth, int forceCount) {
		if (rows.size()==1) {
			// only calculate preferred empty space if it's a single row
			RowImpl r = rows.get(0);
			cost += preferredSpaceCost(r.getWidth()-r.getLeaderSpace(), cellWidth);
		} else if (rows.size()>1) {
			double rc = rows.size()-1;
			// cost is increased with a longer last line, because we prefer if the row count goes down
			RowImpl r = rows.get(rows.size()-1);
			rc += ((r.getWidth()-r.getLeaderSpace())/(double)cellWidth);
			cost += rc/10d;
		} else {
			cost += preferredSpaceCost(0, cellWidth);
		}
		cost += 10*forceCount;
	}

	private double preferredSpaceCost(int r, int cellWidth) { 
		return Math.abs((cellWidth-r)-spacePreferred)/(double)cellWidth;
	}

	@Override
	public void completeTable(List<RowImpl> rows, int columnCount) {
		cost += columnCount*rows.size();
	}

}
