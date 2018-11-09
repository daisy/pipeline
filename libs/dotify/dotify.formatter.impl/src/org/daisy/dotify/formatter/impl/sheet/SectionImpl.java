package org.daisy.dotify.formatter.impl.sheet;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.formatter.impl.writer.Page;
import org.daisy.dotify.formatter.impl.writer.Section;

class SectionImpl implements Section {
	private final List<Page> pages;
	private final SectionProperties props;

	public SectionImpl(SectionProperties props) {
		this.pages = new ArrayList<>();
		this.props = props;
	}
	
	void addPage(Page p) {
		pages.add(p);
	}

	@Override
	public SectionProperties getSectionProperties() {
		return props;
	}

	@Override
	public List<? extends Page> getPages() {
		return pages;
	}

}
