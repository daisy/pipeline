package org.daisy.dotify.formatter.impl;

import java.util.List;

import org.daisy.dotify.api.writer.SectionProperties;

public interface Section {

	public SectionProperties getSectionProperties();
	public List<? extends Page> getPages();
}
