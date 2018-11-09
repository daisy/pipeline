package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.api.translator.Border;

class TableCellInfo {
	private final int rowSpan, colSpan;
	private final Border border;
	private final GridPoint start, end;

	TableCellInfo(TableCellProperties props, GridPoint start) {
		this.start = start;
		this.rowSpan = props.getRowSpan();
		this.colSpan = props.getColSpan();
		this.border = props.getBorder();
		this.end = new GridPoint(start.getRow()+rowSpan-1, start.getCol()+colSpan-1);
	}

	int getRowSpan() {
		return rowSpan;
	}

	int getColSpan() {
		return colSpan;
	}

	Border getBorder() {
		return border;
	}
	
	/**
	 * Starting point, inclusive
	 * @return
	 */
	GridPoint getStartingPoint() {
		return start;
	}
	
	/**
	 * End point, inclusive.
	 * @return
	 */
	GridPoint getEndPoint() {
		return end;
	}

}
