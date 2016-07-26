package org.daisy.dotify.formatter.impl;

import java.util.List;

class AnchorData {
	private final int pageIndex;
	private final List<String> refs;
	
	AnchorData(int pageIndex, List<String> refs) {
		this.pageIndex = pageIndex;
		this.refs = refs;
	}

	int getPageIndex() {
		return pageIndex;
	}
	
	List<String> getAnchors() {
		return refs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pageIndex;
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
		if (pageIndex != other.pageIndex) {
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
