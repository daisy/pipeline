package org.daisy.dotify.formatter.impl.search;

public final class BlockAddress {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blockNumber;
		result = prime * result + (int) (groupNumber ^ (groupNumber >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BlockAddress other = (BlockAddress) obj;
		if (blockNumber != other.blockNumber) {
			return false;
		}
		if (groupNumber != other.groupNumber) {
			return false;
		}
		return true;
	}

}
