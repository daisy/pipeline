package org.daisy.dotify.formatter.impl.search;

public class BlockAddress {
	private static long nextGroupNumber = 0;
	private final long groupNumber;
	private final int blockNumber;

	public BlockAddress(long groupNumber, int blockNumber) {
		this.groupNumber = groupNumber;
		this.blockNumber = blockNumber;
	}
	
	public static synchronized long getNextGroupNumber() {
		nextGroupNumber++;
		return nextGroupNumber;
	}

	public long getGroupNumber() {
		return groupNumber;
	}

	public int getBlockNumber() {
		return blockNumber;
	}

}
