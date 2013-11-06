package org.daisy.dotify.api.formatter;

import java.util.List;


/**
 * Provides an interface for formatting content that varies depending on the 
 * volume number and volume count in which it is placed.
 * 
 * @author Joel Håkansson
 */
public interface VolumeContentFormatter {
	
	/**
	 * Gets the volume max size based on the supplied information.
	 * 
	 * @param volumeNumber the volume number, one based
	 * @param volumeCount the number of volumes
	 * @return returns the maximum number of sheets in the volume
	 */
	public int getVolumeMaxSize(int volumeNumber, int volumeCount);
	
	/**
	 * Formats the pre-volume contents for the specified volume given the supplied information.
	 * @param volumeNumber the volume number, one based
	 * @param volumeCount the number of volumes
	 * @param crh the cross references available
	 * @return returns the formatted contents
	 */
	public List<Iterable<BlockSequence>> formatPreVolumeContents(int volumeNumber, int volumeCount, CrossReferences crh);
	
	/**
	 * Formats the post-volume contents for the specified volume given the supplied information.
	 * @param volumeNumber the volume number, one based
	 * @param volumeCount the number of volumes
	 * @param crh the cross references available
	 * @return returns the formatted contents
	 */
	public List<Iterable<BlockSequence>> formatPostVolumeContents(int volumeNumber, int volumeCount, CrossReferences crh);
	
}
