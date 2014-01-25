package org.daisy.dotify.api.formatter;

/**
 * Provides methods needed to build volume anchored content.
 * 
 * @author Joel Håkansson
 */
public interface VolumeContentBuilder {
	
	/**
	 * Creates a new sequence at the current position.
	 *  
	 * @param props the properties of the sequence
	 * @return returns a formatter core that can be used to add additional elements
	 */
	public FormatterCore newSequence(SequenceProperties props);

	/**
	 * Creates a new toc sequence at the current position.
	 * 
	 * @param props the properties of the toc sequence
	 * @return returns a toc sequence builder
	 */
	public TocSequenceBuilder newTocSequence(TocProperties props);

}
