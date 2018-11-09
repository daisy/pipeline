package org.daisy.dotify.formatter.impl.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

class TableData implements Iterable<TableRow> {
	private static final Logger logger = Logger.getLogger(TableData.class.getCanonicalName());
	private final Stack<TableRow> rows;
	private final Map<GridPoint, TableCell> grid;
	private int gy, cMax;
	private final FormatterCoreContext fc;
	private String TABLE_ERROR_MSG = "There's an error in the table model that cannot be fixed automatically. "
			+ "Go back to the source and correct any errors in the table.";

	TableData(FormatterCoreContext fc) {
		this.fc = fc;
		rows = new Stack<>();
		grid = new HashMap<>();
		cMax = 0;
		gy = 0;
	}
	
	void beginsTableRow() {
		if (!rows.empty()) {
			rows.peek().endsTableCell();
		}
		gy += rowHeight();
		TableRow ret = new TableRow(fc);
		rows.add(ret);
		
	}
	
	int rowHeight() {
		return (rows.size()>0?rows.peek().getRowHeight():0);
	}
	
	int maxRowSpan() {
		return (rows.size()>0?rows.peek().getMaxCellHeight():0);
	}

	FormatterCore beginsTableCell(TableCellProperties props) {
		int r = gy;
		int c = rows.peek().cellCount(); // this is just a starting point, we know for sure that c cannot be less than this
		while (grid.get(new GridPoint(r, c))!=null) {
			c++;
		}
		cMax = Math.max(cMax, c+props.getColSpan());
		return beginsTableCell(props, r, c);
	}
	
	private FormatterCore beginsTableCell(TableCellProperties props, int r, int c) {
		TableCell ret = rows.peek().beginsTableCell(props, new GridPoint(r, c));
		setGrid(ret, props);
		return ret;
	}
	
	private void setGrid(TableCell ret, TableCellProperties props) {
		int r = ret.getInfo().getStartingPoint().getRow();
		int c = ret.getInfo().getStartingPoint().getCol();
		for (int i=0; i<props.getRowSpan(); i++) {
			for (int j=0; j<props.getColSpan(); j++) {
				GridPoint p = new GridPoint(r+i, c+j);
				if (grid.containsKey(p)) {
					//TODO: throw checked exception? OR auto fix table
					throw new RuntimeException("Conflicting col-span/row-span.");
				}
				grid.put(p, ret);
			}
		}
	}
	
	void closeTable() {
		for (int i=0; i<rows.size(); i++) {
			completeRow(rows.get(i));
		}
	}
	
	private void completeRow(TableRow row) {
		if (row.cellCount()==0) {
			throw new UnsupportedOperationException(TABLE_ERROR_MSG);
		}
		TableCell last = row.getCell(row.cellCount()-1);
		final int firstCol = last.getInfo().getEndPoint().getCol()+1;
		final int firstRow = last.getInfo().getStartingPoint().getRow();
		
		int gw = getGridWidth()-firstCol;
		if (gw>0) {
			int count=0;
			int empty=0;
			for (int gc=firstCol; gc<getGridWidth(); gc++) {
				for (int gr=firstRow; gr<firstRow+row.getRowHeight(); gr++) {
					count++;
					if (cellForGrid(gr, gc)==null) {
						empty++;
					};
				}
			}
			if (empty==0) {
				//ok!
			} else {
				logger.warning("There is an error in the table model. No table cell was found at row " + (firstRow+1) + ", column " + (firstCol+1)
						+ " (note that row/column is expressed as one-based grid coordinates, which can differ from the data source's view "
						+ "of rows and columns).");
				if (count==empty) { // all empty
					//simple
					TableCellProperties props = new TableCellProperties.Builder().rowSpan(row.getRowHeight()).colSpan(gw).build();
					TableCell tc = row.beginsTableCell(props, new GridPoint(firstRow, firstCol));
					setGrid(tc, props);
				} else {
					//tricky
					throw new UnsupportedOperationException(TABLE_ERROR_MSG);
				}
			}
		}
	}
	
	TableCell getCurrentCell() {
		return rows.peek().getCurrentCell();
	}
	
	TableCell cellForGrid(int r, int c) {
		return cellForGrid(new GridPoint(r, c));
	}
	
	TableCell cellForGrid(GridPoint g) {
		return grid.get(g);
	}
	
	void setCellForGrid(TableCell tc, GridPoint g) {
		grid.put(g, tc);
	}
	
	TableCell cellForIndex(int r, int c) {
		return getRow(r).getCell(c);
	}
	 
	int getRowCount() {
		return rows.size();
	}
	
	boolean isEmpty() {
		return rows.isEmpty();
	}
	
	TableRow getRow(int i) {
		return rows.get(i);
	}

	@Override
	public Iterator<TableRow> iterator() {
		return rows.iterator();
	}

	public int getGridHeight() {
		return gy+maxRowSpan();
	}
	
	public int getGridWidth() {
		return cMax;
	}

}
