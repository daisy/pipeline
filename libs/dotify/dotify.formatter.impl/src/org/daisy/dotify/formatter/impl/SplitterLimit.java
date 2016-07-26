package org.daisy.dotify.formatter.impl;

public interface SplitterLimit {
	
	/**
	 * Gets the splitter limit for the volume index.
	 * @param volume the volume index (one based)
	 * @return returns the maximum sheets in the volume
	 */
	public int getSplitterLimit(int volume);

}
