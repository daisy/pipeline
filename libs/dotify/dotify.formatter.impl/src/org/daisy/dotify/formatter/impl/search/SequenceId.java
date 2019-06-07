package org.daisy.dotify.formatter.impl.search;

public class SequenceId {
	private final int ordinal;
	private final DocumentSpace space;
	private final Integer volumeGroup;
	
	public SequenceId(int ordinal, DocumentSpace space, Integer volumeGroup) {
		if ((space.equals(DocumentSpace.BODY) && volumeGroup == null)
		    || (!space.equals(DocumentSpace.BODY) && volumeGroup != null)) {
			throw new IllegalArgumentException();
		}
		this.ordinal = ordinal;
		this.space = space;
		this.volumeGroup = volumeGroup;
	}
	
	int getOrdinal() {
		return ordinal;
	}
	
	DocumentSpace getSpace() {
		return space;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ordinal;
		result = prime * result + ((space == null) ? 0 : space.hashCode());
		result = prime * result + ((volumeGroup == null) ? 0 : volumeGroup.hashCode());
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
		SequenceId other = (SequenceId) obj;
		if (ordinal != other.ordinal) {
			return false;
		}
		if (space == null) {
			if (other.space != null) {
				return false;
			}
		} else if (!space.equals(other.space)) {
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
		return "SequenceId [ordinal=" + ordinal + ", space=" + space
			+ (volumeGroup == null ? "" : ", volumeGroup=" + volumeGroup)
			+ "]";
	}

}
