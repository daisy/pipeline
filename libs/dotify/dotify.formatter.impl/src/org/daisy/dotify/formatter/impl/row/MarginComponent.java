package org.daisy.dotify.formatter.impl.row;

public class MarginComponent {
	private final String border;
	private final int outer;
	private final int inner;

	public MarginComponent(String border, int outerOffset, int innerOffset) {
		this.border = border;
		this.outer = outerOffset;
		this.inner = innerOffset;
	}

	String getBorder() {
		return border;
	}

	int getOuterOffset() {
		return outer;
	}
	
	int getInnerOffset() {
		return inner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((border == null) ? 0 : border.hashCode());
		result = prime * result + inner;
		result = prime * result + outer;
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
		MarginComponent other = (MarginComponent) obj;
		if (border == null) {
			if (other.border != null) {
				return false;
			}
		} else if (!border.equals(other.border)) {
			return false;
		}
		if (inner != other.inner) {
			return false;
		}
		if (outer != other.outer) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MarginComponent [border=" + border + ", outer=" + outer + ", inner=" + inner + "]";
	}

}
