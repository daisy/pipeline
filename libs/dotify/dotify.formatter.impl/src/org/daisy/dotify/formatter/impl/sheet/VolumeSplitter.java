package org.daisy.dotify.formatter.impl.sheet;

/**
 * Provides a volume splitter.
 * 
 * @author Joel Håkansson
 *
 */
public interface VolumeSplitter {

	/**
	 * Sets the number of sheets to distribute and adjusts the number of volumes based on
	 * the number of sheets that did not fit in the volumes of the previous iteration.
	 *
	 * @param sheets the total number of sheets
	 * @param sheetsRemaining the number of sheets that did not fit in the previous iteration
	 */
	void updateSheetCount(int sheets, int sheetsRemaining);

	/**
	 * Gets the number of sheets in a volume.
	 * 
	 * @param volIndex volume index, one-based
	 * @return returns the number of sheets in the volume
	 */
	public int sheetsInVolume(int volIndex);

	/**
	 * Gets the number of volumes required according to this splitter.
	 * @return returns the number of volumes required
	 */
	int getVolumeCount();
}
