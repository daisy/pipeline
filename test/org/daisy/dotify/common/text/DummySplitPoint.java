package org.daisy.dotify.common.text;
class DummySplitPoint implements SplitPointUnit {
	private final boolean breakable, skippable, collapsable;
	private final float size;
	static class Builder {
		boolean breakable = false; boolean skippable = false; boolean collapsable = false; float size = 1;
		Builder() {
		}
		Builder breakable(boolean value) { breakable = value; return this; }
		Builder skippable(boolean value) { skippable = value; return this; }
		Builder collapsable(boolean value) { collapsable = value; return this; }
		Builder size(float value) { size = value; return this; }
		DummySplitPoint build() {
			return new DummySplitPoint(this);
		}
	}

	DummySplitPoint(Builder builder) {
		this.breakable = builder.breakable;
		this.skippable = builder.skippable;
		this.size = builder.size;
		this.collapsable = builder.collapsable;
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
	public boolean isCollapsable() {
		return collapsable;
	}
}