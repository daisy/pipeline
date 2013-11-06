package org.daisy.dotify.api.formatter;



/**
 * Provides a container for a physical volume of braille
 * @author Joel Håkansson
 */
public interface Volume {

	/**
	 * Gets the contents
	 * @return returns the contents
	 */
	public Iterable<? extends PageSequence> getContents();
	
}
