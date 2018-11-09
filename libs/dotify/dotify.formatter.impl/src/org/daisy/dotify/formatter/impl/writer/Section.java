package org.daisy.dotify.formatter.impl.writer;

import java.util.List;

import org.daisy.dotify.api.writer.SectionProperties;

/**
 * Provides a section of braille pages having the same properties.
 * @author Joel Håkansson
 *
 */
public interface Section {

	/**
	 * Gets the section properties.
	 * @return the properties for the section
	 */
	public SectionProperties getSectionProperties();

	/**
	 * Gets the pages in the section
	 * @return returns a list of pages
	 */
	public List<? extends Page> getPages();
}
