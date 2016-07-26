package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.formatter.impl.DefaultContext.Space;

class SheetIdentity {
	private final Space space;
	private final int volumeIndex;
	private final int sheetIndex;

	public SheetIdentity(Space s, int volumeIndex, int sheetIndex) {
		this.space = s;
		this.volumeIndex = volumeIndex;
		this.sheetIndex = sheetIndex;
	}

	public Space getSpace() {
		return space;
	}

	public int getVolumeIndex() {
		return volumeIndex;
	}

	public int getSheetIndex() {
		return sheetIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((space == null) ? 0 : space.hashCode());
		result = prime * result + sheetIndex;
		result = prime * result + volumeIndex;
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
		SheetIdentity other = (SheetIdentity) obj;
		if (space != other.space) {
			return false;
		}
		if (sheetIndex != other.sheetIndex) {
			return false;
		}
		if (volumeIndex != other.volumeIndex) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SheetIdentity [space=" + space + ", volumeIndex=" + volumeIndex + ", sheetIndex=" + sheetIndex + "]";
	}

}
