package org.daisy.dotify.formatter.impl;

import java.lang.ref.WeakReference;

import org.daisy.dotify.api.writer.SectionProperties;

/**
 * Provides a sequence of pages.
 * 
 * @author Joel Håkansson
 */
class PageSequence extends PageView implements Section {
	private final WeakReference<PageStruct> parent;
	private final LayoutMaster master;
	private final int pageOffset;
	
	PageSequence(PageStruct parent, LayoutMaster master, int pageOffset) { //, int pageOffset, FormatterFactory formatterFactory) {
		super(parent.getPages(), parent.getPages().size());
		this.parent = new WeakReference<>(parent);
		this.master = master;
		this.pageOffset = pageOffset;
	}
	
	void addPage(PageImpl p) {
		pages.add(p);
		toIndex++;
	}
	
	PageStruct getParent() {
		return parent.get();
	}

	/**
	 * Gets the layout master for this sequence
	 * @return returns the layout master for this sequence
	 */
	public LayoutMaster getLayoutMaster() {
		return master;
	}

	
	int currentPageNumber() {
		return peek().getPageIndex()+1;
	}
	
	public int getPageNumberOffset() {
		return pageOffset;
	}

	@Override
	public SectionProperties getSectionProperties() {
		return master;
	}

}
