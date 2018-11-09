package org.daisy.dotify.formatter.impl.search;

public class SequenceId {
	private final int ordinal;
	private final DocumentSpace space;
	
	public SequenceId(int ordinal, DocumentSpace space) {
		this.ordinal = ordinal;
		this.space = space;
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
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SequenceId other = (SequenceId) obj;
		if (ordinal != other.ordinal)
			return false;
		if (space == null) {
			if (other.space != null)
				return false;
		} else if (!space.equals(other.space))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SequenceId [ordinal=" + ordinal + ", space=" + space + "]";
	}

}
