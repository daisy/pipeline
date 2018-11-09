package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.BlockProperties;

class AncestorContext {
	private final BlockProperties blockProperties;
	private final Integer volumeKeepPriority;
	private int listIterator;
	
	AncestorContext(BlockProperties props, Integer volumeKeep) {
		this.blockProperties = props;
		this.volumeKeepPriority = volumeKeep;
		this.listIterator = 0;
	}
	public BlockProperties getBlockProperties() {
		return blockProperties;
	}

	public Integer getVolumeKeepPriority() {
		return volumeKeepPriority;
	}
	public int nextListNumber() {
		listIterator++;
		return listIterator;
	}
	public void setListNumber(int value) {
		listIterator = value;
	}
}