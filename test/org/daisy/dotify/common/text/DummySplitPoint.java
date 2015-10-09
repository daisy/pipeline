package org.daisy.dotify.common.text;
class DummySplitPoint implements SplitPointUnit {
	private final boolean breakable, skippable, collapsible;
	private final float size;
	static class Builder {
		boolean breakable = false; boolean skippable = false; boolean collapsible = false; float size = 1;
		Builder() {
		}
		Builder breakable(boolean value) { breakable = value; return this; }
		Builder skippable(boolean value) { skippable = value; return this; }
		Builder collapsable(boolean value) { collapsible = value; return this; }
		Builder size(float value) { size = value; return this; }
		DummySplitPoint build() {
			return new DummySplitPoint(this);
		}
	}

	DummySplitPoint(Builder builder) {
		this.breakable = builder.breakable;
		this.skippable = builder.skippable;
		this.size = builder.size;
		this.collapsible = builder.collapsible;
	}
	@Override
	public boolean isBreakable() {
		return breakable;
	}
	@Override
	public boolean isSkippable() {
		return skippable;
	}
	@Override
	public float getUnitSize() {
		return size;
	}
	@Override
	public boolean isCollapsible() {
		return collapsible;
	}

	@Override
	public boolean collapsesWith(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DummySplitPoint other = (DummySplitPoint) obj;
		if (collapsible != other.collapsible) {
			return false;
		}
		return true;
	}
	
	
}