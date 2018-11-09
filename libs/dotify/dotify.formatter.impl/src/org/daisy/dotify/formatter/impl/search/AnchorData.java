package org.daisy.dotify.formatter.impl.search;

import java.util.List;

public class AnchorData {
	private final int pageNumber;
	private final List<String> refs;
	
	public AnchorData(List<String> refs, int pageNumber) {
		this.pageNumber = pageNumber;
		this.refs = refs;
	}

	public int getPageNumber() {
		return pageNumber;
	}
	
	public List<String> getAnchors() {
		return refs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pageNumber;
		result = prime * result + ((refs == null) ? 0 : refs.hashCode());
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
		AnchorData other = (AnchorData) obj;
		if (pageNumber != other.pageNumber) {
			return false;
		}
		if (refs == null) {
			if (other.refs != null) {
				return false;
			}
		} else if (!refs.equals(other.refs)) {
			return false;
		}
		return true;
	}

}
