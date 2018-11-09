package org.daisy.dotify.formatter.impl.search;

public class SheetIdentity {
	private final Space space;
	private final Integer volumeIndex;
	private final Integer volumeGroup;
	private final int sheetIndex;

	public SheetIdentity(Space s, Integer volumeIndex, Integer volumeGroup, int sheetIndex) {
		this.space = s;
		if ((space == Space.BODY && (volumeIndex != null || volumeGroup == null))
		    || (space != Space.BODY && (volumeIndex == null || volumeGroup != null))) {
			throw new IllegalArgumentException();
		}
		this.volumeIndex = volumeIndex;
		this.volumeGroup = volumeGroup;
		this.sheetIndex = sheetIndex;
	}

	public Space getSpace() {
		return space;
	}

	public Integer getVolumeIndex() {
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
		result = prime * result + (volumeIndex == null ? 0 : volumeIndex);
		result = prime * result + (volumeGroup == null ? 0 : volumeGroup);
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
		if (volumeIndex == null) {
			if (other.volumeIndex != null) {
				return false;
			}
		} else if (!volumeIndex.equals(other.volumeIndex)) {
			return false;
		}
		if (volumeGroup == null) {
			if (other.volumeGroup != null) {
				return false;
			}
		} else if (!volumeGroup.equals(other.volumeGroup)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SheetIdentity [space=" + space
			+ (volumeIndex == null ? "" : ", volumeIndex=" + volumeIndex)
			+ (volumeGroup == null ? "" : ", volumeGroup=" + volumeGroup)
			+ ", sheetIndex=" + sheetIndex + "]";
	}

}
