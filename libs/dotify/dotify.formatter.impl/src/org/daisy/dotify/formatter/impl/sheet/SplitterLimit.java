package org.daisy.dotify.formatter.impl.sheet;

/**
 * Provides the splitter limit for a given volume.
 * @author Joel Håkansson
 *
 */
public interface SplitterLimit {
	
	/**
	 * Gets the splitter limit for the volume index.
	 * @param volume the volume index (one based)
	 * @return returns the maximum sheets in the volume
	 */
	public int getSplitterLimit(int volume);

}
