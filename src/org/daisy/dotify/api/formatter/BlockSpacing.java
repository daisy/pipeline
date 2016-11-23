package org.daisy.dotify.api.formatter;

/**
 * Provides a spacing definition for blocks.
 * @author Joel Håkansson
 */
public final class BlockSpacing {
	private final int left;
	private final int right;
	private final int top;
	private final int bottom;
	
	/**
	 * Provides a builder for block spacing.
	 */
	public static final class Builder {
		int left = 0;
		int right = 0;
		int top = 0;
		int bottom = 0;
		/**
		 * Set the left spacing for the block, in characters.
		 * @param leftSpacing left spacing, in characters
		 * @return returns "this" object
		 */
		public Builder leftSpacing(int leftSpacing) {
			this.left = leftSpacing;
			return this;
		}
		
		/**
		 * Set the right spacing for the block, in characters.
		 * @param rightSpacing right spacing, in characters
		 * @return returns "this" object
		 */
		public Builder rightSpacing(int rightSpacing) {
			this.right = rightSpacing;
			return this;
		}
		
		/**
		 * Set the top spacing for the block, in characters.
		 * @param topSpacing top spacing, in characters
		 * @return returns "this" object
		 */
		public Builder topSpacing(int topSpacing) {
			this.top = topSpacing;
			return this;
		}
		
		/**
		 * Set the bottom spacing for the block, in characters.
		 * @param bottomSpacing bottom spacing, in characters
		 * @return returns "this" object
		 */
		public Builder bottomSpacing(int bottomSpacing) {
			this.bottom = bottomSpacing;
			return this;
		}
		
		/**
		 * Build BlockSpacing using the current state of the Builder.
		 * @return returns a new BlockSpacing instance
		 */
		public BlockSpacing build() {
			return new BlockSpacing(this);
		}
	}
	
	private BlockSpacing(Builder builder) {
		left = builder.left;
		right = builder.right;
		top = builder.top;
		bottom = builder.bottom;
	}
	
	/**
	 * Get left spacing, in characters
	 * @return returns the left spacing
	 */
	public int getLeftSpacing() {
		return left;
	}
	
	/**
	 * Get right spacing, in characters
	 * @return returns the right spacing
	 */
	public int getRightSpacing() {
		return right;
	}
	
	/**
	 * Get top spacing, in characters
	 * @return returns the top spacing
	 */
	public int getTopSpacing() {
		return top;
	}
	
	/**
	 * Get bottom spacing, in characters
	 * @return returns the bottom spacing
	 */
	public int getBottomSpacing() {
		return bottom;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bottom;
		result = prime * result + left;
		result = prime * result + right;
		result = prime * result + top;
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
		BlockSpacing other = (BlockSpacing) obj;
		if (bottom != other.bottom) {
			return false;
		}
		if (left != other.left) {
			return false;
		}
		if (right != other.right) {
			return false;
		}
		if (top != other.top) {
			return false;
		}
		return true;
	}

}
