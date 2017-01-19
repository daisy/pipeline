package org.daisy.dotify.common.split;

import java.util.ArrayList;
import java.util.List;

class DummySplitPoint implements SplitPointUnit {
	private final boolean breakable, skippable, collapsible;
	private final float size;
	private final float minSize;
	private final List<String> supplementIds;
	static class Builder {
		boolean breakable = false; boolean skippable = false; boolean collapsible = false; float size = 1; Float minSize = null;
		List<String> supplementIds;
		Builder() {
			supplementIds = new ArrayList<>();
		}
		Builder breakable(boolean value) { breakable = value; return this; }
		Builder skippable(boolean value) { skippable = value; return this; }
		Builder collapsable(boolean value) { collapsible = value; return this; }
		Builder size(float value) { size = value; return this; }
		Builder minSize(float value) { minSize = value; return this; }
		Builder supplementID(String id) { supplementIds.add(id); return this; }
		DummySplitPoint build() {
			return new DummySplitPoint(this);
		}
	}

	DummySplitPoint(Builder builder) {
		this.breakable = builder.breakable;
		this.skippable = builder.skippable;
		this.size = builder.size;
		if (builder.minSize==null) {
			this.minSize = size;
		} else {
			this.minSize = builder.minSize;
		}
		this.collapsible = builder.collapsible;
		this.supplementIds = builder.supplementIds;
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
	@Override
	public List<String> getSupplementaryIDs() {
		return supplementIds;
	}
	@Override
	public float getLastUnitSize() {
		return minSize;
	}
	
	
}