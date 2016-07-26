package org.daisy.dotify.formatter.impl;

public interface Volume {

	/**
	 * Gets the contents
	 * @return returns the contents
	 */
	public Iterable<? extends Section> getSections();
}
