package org.daisy.dotify.formatter.impl.search;

public final class BlockLineLocation {
	private final BlockAddress blockAddress;
	private final int lineNumber;

	public BlockLineLocation(BlockAddress block, int line) {
		this.blockAddress = block;
		this.lineNumber = line;
	}

	public BlockAddress getBlockAddress() {
		return blockAddress;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockAddress == null) ? 0 : blockAddress.hashCode());
		result = prime * result + lineNumber;
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
		BlockLineLocation other = (BlockLineLocation) obj;
		if (blockAddress == null) {
			if (other.blockAddress != null) {
				return false;
			}
		} else if (!blockAddress.equals(other.blockAddress)) {
			return false;
		}
		if (lineNumber != other.lineNumber) {
			return false;
		}
		return true;
	}	

}