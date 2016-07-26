package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.dotify.common.layout.SplitPointUnit;
class Sheet implements SplitPointUnit {
	private final static List<String> SUPPLEMENTS = Collections.unmodifiableList(new ArrayList<String>()); 
	private final List<PageImpl> pages;
	private final boolean breakable, skippable, collapsible;
	private final boolean startNewVolume;
	private final Integer avoidVolumeBreakAfterPriority;
	
	static class Builder {
		private final List<PageImpl> pages;
		private boolean breakable = false;
		private boolean startNewVolume = false;
		private Integer avoidVolumeBreakAfterPriority = null;
		
		Builder() {
			pages = new ArrayList<>();
		}
	
		Builder add(PageImpl value) {
			pages.add(value);
			return this;
		}
		Builder addAll(List<PageImpl> value) {
			pages.addAll(value);
			return this;
		}

		Builder breakable(boolean value) {
			this.breakable = value;
			return this;
		}
		
		Builder startNewVolume(boolean value) {
			this.startNewVolume = value;
			return this;
		}

		Builder avoidVolumeBreakAfterPriority(Integer value) {
			this.avoidVolumeBreakAfterPriority = value;
			return this;
		}

		Sheet build() {
			return new Sheet(this);
		}
	}

	private Sheet(Builder builder) {
		if (builder.pages.size()>2) {
			throw new IllegalArgumentException("A sheet can not contain more than two pages.");
		}
		this.pages = Collections.unmodifiableList(new ArrayList<>(builder.pages));
		this.breakable = builder.breakable && builder.avoidVolumeBreakAfterPriority==null;
		this.avoidVolumeBreakAfterPriority = builder.avoidVolumeBreakAfterPriority;
		this.skippable = pages.isEmpty();
		this.collapsible = pages.isEmpty();
		this.startNewVolume = builder.startNewVolume;
	}
	
	List<PageImpl> getPages() {
		return pages;
	}
	
	boolean shouldStartNewVolume() {
		return startNewVolume;
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
	public boolean isCollapsible() {
		return collapsible;
	}

	@Override
	public float getUnitSize() {
		return 1;
	}

	@Override
	public float getLastUnitSize() {
		return 1;
	}
	
	Integer getAvoidVolumeBreakAfterPriority() {
		return avoidVolumeBreakAfterPriority;
	}

	@Override
	public boolean collapsesWith(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else  if (getClass() != obj.getClass()) {
			return false;
		} else {
			Sheet other = (Sheet) obj;
			return this.isCollapsible() && other.isCollapsible();
		}
	}

	@Override
	public List<String> getSupplementaryIDs() {
		return SUPPLEMENTS;
	}

	@Override
	public String toString() {
		return "Sheet [pages=" + pages + ", breakable=" + breakable + ", skippable=" + skippable
				+ ", collapsible=" + collapsible + "]";
	}

}