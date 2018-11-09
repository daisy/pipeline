package org.daisy.dotify.formatter.impl.search;

/**
 * <p>Provides coordinates for a coherent document space. Each volume consists of 
 * three coordinates representing three distinct search spaces where markers 
 * can be found in the largest scope (called "document"). In other words, a search 
 * for markers cannot retrieve something outside of the search's document space
 * coordinates.</p>
 * 
 * <p>For this reason, the whole of the main text flow is in the same document
 * space, that is to say it is not qualified with a volume number.</p>
 *  
 * @author Joel Håkansson
 */
public final class DocumentSpace {
	static final DocumentSpace BODY = new DocumentSpace(Space.BODY, null);
	private final int volumeNumber;
	private final Space space;

	/**
	 * Note that Space.BODY ignores volumeNumber
	 * @param space the space
	 * @param volumeNumber the volume number
	 */
	public DocumentSpace(Space space, Integer volumeNumber) {
		this.space = space;
		this.volumeNumber = (volumeNumber==null||space==Space.BODY?0:volumeNumber);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((space == null) ? 0 : space.hashCode());
		result = prime * result + volumeNumber;
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
		DocumentSpace other = (DocumentSpace) obj;
		if (space != other.space)
			return false;
		if (volumeNumber != other.volumeNumber)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DocumentSpace [volumeNumber=" + volumeNumber + ", space=" + space + "]";
	}
	
}
