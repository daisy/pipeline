package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.splitter.SplitPointUnit;
import org.daisy.dotify.formatter.impl.datatype.VolumeKeepPriority;
import org.daisy.dotify.formatter.impl.page.PageImpl;
public class Sheet implements SplitPointUnit {
	private static final List<String> SUPPLEMENTS = Collections.unmodifiableList(new ArrayList<String>());
	private final SectionProperties master;
	private final List<PageImpl> pages;
	private final boolean breakable, skippable, collapsible;
	private final VolumeKeepPriority avoidVolumeBreakAfterPriority;
	
	static class Builder {
		private final SectionProperties sectionProperties;
		private final List<PageImpl> pages;
		private boolean breakable = false;
		private VolumeKeepPriority avoidVolumeBreakAfterPriority = VolumeKeepPriority.empty();

		/**
		 * Creates a new builder.
		 * @param props the section properties. Note that this object is used as
		 * section separator, meaning that each new section MUST have a separate
		 * object even if the data is the same.
		 */
		Builder(SectionProperties props) {
			this.sectionProperties = props;
			this.pages = new ArrayList<>();
		}
		
		Builder(Builder template) {
			this.sectionProperties = template.sectionProperties;
			this.pages = new ArrayList<>(template.pages);
			this.breakable = template.breakable;
			this.avoidVolumeBreakAfterPriority = template.avoidVolumeBreakAfterPriority;
		}
		
		static Builder copyUnlessNull(Builder template) {
			return (template==null?null:new Builder(template));
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

		Builder avoidVolumeBreakAfterPriority(VolumeKeepPriority value) {
			this.avoidVolumeBreakAfterPriority = Objects.requireNonNull(value);
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
		this.master = builder.sectionProperties;
		this.pages = Collections.unmodifiableList(new ArrayList<>(builder.pages));
		this.breakable = builder.breakable && !builder.avoidVolumeBreakAfterPriority.hasValue();
		this.avoidVolumeBreakAfterPriority = builder.avoidVolumeBreakAfterPriority;
		this.skippable = pages.isEmpty();
		this.collapsible = pages.isEmpty();
	}
	
	public SectionProperties getSectionProperties() {
		return master;
	}
	
	public List<PageImpl> getPages() {
		return pages;
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
	
	public VolumeKeepPriority getAvoidVolumeBreakAfterPriority() {
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
	
	
	/**
	 * Counts the number of pages
	 * @param sheets the list of sheets to count
	 * @return returns the number of pages
	 */
	public static int countPages(List<Sheet> sheets) {
		return sheets.stream().mapToInt(s -> s.getPages().size()).sum();
	}
	
	static String toDebugBreakableString(List<Sheet> units) {
		StringBuilder debug = new StringBuilder();
		for (Sheet s : units) {
			debug.append("s");
			if (s.isBreakable()) {
				debug.append("-");
			}
		}
		return debug.toString();
	}


}