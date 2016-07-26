package org.daisy.dotify.formatter.impl;

interface VolumeSplitter {

	void updateSheetCount(int sheets);
	
	/**
	 * Gets the number of sheets in a volume
	 * @param volIndex volume index, one-based
	 * @return returns the number of sheets in the volume
	 */
	public int sheetsInVolume(int volIndex);
	
	void adjustVolumeCount(int sheets);
}
