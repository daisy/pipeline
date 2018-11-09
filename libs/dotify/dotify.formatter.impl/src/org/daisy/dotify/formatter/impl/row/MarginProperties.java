package org.daisy.dotify.formatter.impl.row;
/**
 * Provides properties for a margin. {@link MarginProperties} are
 * immutable.
 * @author Joel Håkansson
 */
public final class MarginProperties {
	static final MarginProperties EMPTY_MARGIN = new MarginProperties();
	private final String margin;
	private final boolean spaceOnly;
	
	MarginProperties() {
		this("", true);
	}
	
	public MarginProperties(String margin, boolean spaceOnly) {
		this.margin = margin;
		this.spaceOnly = spaceOnly;
	}
	
	/**
	 * Creates a new margin with the supplied margin appended. 
	 * @param mp the margin to append
	 * @return the new margin
	 */
	public MarginProperties append(MarginProperties mp) {
		return new MarginProperties(getContent()+mp.getContent(), isSpaceOnly()&&mp.isSpaceOnly());
	}

	public String getContent() {
		return margin;
	}

	@Override
	public String toString() {
		return margin;
	}

	public boolean isSpaceOnly() {
		return spaceOnly;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((margin == null) ? 0 : margin.hashCode());
		result = prime * result + (spaceOnly ? 1231 : 1237);
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
		MarginProperties other = (MarginProperties) obj;
		if (margin == null) {
			if (other.margin != null) {
				return false;
			}
		} else if (!margin.equals(other.margin)) {
			return false;
		}
		if (spaceOnly != other.spaceOnly) {
			return false;
		}
		return true;
	}

}