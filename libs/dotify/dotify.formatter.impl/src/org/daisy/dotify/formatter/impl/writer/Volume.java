package org.daisy.dotify.formatter.impl.writer;

/**
 * Provides a volume of braille.
 * 
 * @author Joel Håkansson
 */
public interface Volume {

	/**
	 * Gets the contents
	 * @return returns the contents
	 */
	public Iterable<? extends Section> getSections();
}
