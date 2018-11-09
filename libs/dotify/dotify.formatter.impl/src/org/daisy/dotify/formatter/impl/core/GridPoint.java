package org.daisy.dotify.formatter.impl.core;
class GridPoint {
	private final int row,  col;
	
	GridPoint(int r, int c) {
		this.row = r;
		this.col = c;
	}

	int getRow() {
		return row;
	}

	int getCol() {
		return col;
	}

	@Override
	public String toString() {
		return "GridPoint [row=" + row + ", col=" + col + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GridPoint other = (GridPoint) obj;
		if (col != other.col) {
			return false;
		}
		if (row != other.row) {
			return false;
		}
		return true;
	}
	
}