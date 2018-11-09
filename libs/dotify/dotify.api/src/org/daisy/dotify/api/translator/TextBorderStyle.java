package org.daisy.dotify.api.translator;

/**
 * Defines a text border. The border cannot be more than one
 * character high, and shouldn't be more than one character wide.
 * 
 * @author Joel Håkansson
 */
public class TextBorderStyle {
	private final String topLeftCorner, topBorder, topRightCorner, leftBorder,
			rightBorder, bottomLeftCorner, bottomBorder, bottomRightCorner;

	/**
	 * Loaded on first access
	 */
	public static final TextBorderStyle NONE = new Builder().build();

	/*
	 * enum StylePreset {
	 * NONE, SOLID_OUTER
	 * }
	 */

	/**
	 * Provides a builder for text borders.
	 */
	public static class Builder {
		String topLeftCorner, topBorder, topRightCorner, leftBorder,
				rightBorder, bottomLeftCorner, bottomBorder, bottomRightCorner;

		/**
		 * Creates a new text border style builder.
		 */
		public Builder() {
			this.topLeftCorner = "";
			this.topBorder = "";
			this.topRightCorner = "";
			this.leftBorder = "";
			this.rightBorder = "";
			this.bottomLeftCorner = "";
			this.bottomBorder = "";
			this.bottomRightCorner = "";
		}

		/**
		 * Sets the top left corner for the border
		 * 
		 * @param pattern
		 *            top left corner pattern
		 * @return returns this Builder
		 */
		public Builder topLeftCorner(String pattern) {
			this.topLeftCorner = pattern;
			return this;
		}

		/**
		 * Sets the top of the border
		 * 
		 * @param pattern
		 *            the top border pattern
		 * @return returns this Builder
		 */
		public Builder topBorder(String pattern) {
			this.topBorder = pattern;
			return this;
		}

		/**
		 * Sets the top right corner for the border
		 * 
		 * @param pattern
		 *            top right corner pattern
		 * @return returns this Builder
		 */
		public Builder topRightCorner(String pattern) {
			this.topRightCorner = pattern;
			return this;
		}

		/**
		 * Sets the left border pattern
		 * 
		 * @param pattern
		 *            the left border pattern
		 * @return returns this Builder
		 */
		public Builder leftBorder(String pattern) {
			this.leftBorder = pattern;
			return this;
		}

		/**
		 * Sets the right border pattern
		 * 
		 * @param pattern
		 *            the right border pattern
		 * @return returns this Builder
		 */
		public Builder rightBorder(String pattern) {
			this.rightBorder = pattern;
			return this;
		}

		/**
		 * Sets the bottom left corner
		 * 
		 * @param pattern
		 *            bottom left corner pattern
		 * @return returns this Builder
		 */
		public Builder bottomLeftCorner(String pattern) {
			this.bottomLeftCorner = pattern;
			return this;
		}

		/**
		 * Sets the bottom of the border
		 * 
		 * @param pattern
		 *            bottom border pattern
		 * @return returns this Builder
		 */
		public Builder bottomBorder(String pattern) {
			this.bottomBorder = pattern;
			return this;
		}

		/**
		 * Sets the bottom right corner
		 * 
		 * @param pattern
		 *            bottom right corner pattern
		 * @return returns this Builder
		 */
		public Builder bottomRightCorner(String pattern) {
			this.bottomRightCorner = pattern;
			return this;
		}

		/**
		 * Creates a new text border style using the current state
		 * of the builder.
		 * @return returns a new text border style
		 */
		public TextBorderStyle build() {
			return new TextBorderStyle(this);
		}

	}

	private TextBorderStyle(Builder builder) {
		this.topLeftCorner = builder.topLeftCorner;
		this.topBorder = builder.topBorder;
		this.topRightCorner = builder.topRightCorner;
		this.leftBorder = builder.leftBorder;
		this.rightBorder = builder.rightBorder;
		this.bottomLeftCorner = builder.bottomLeftCorner;
		this.bottomBorder = builder.bottomBorder;
		this.bottomRightCorner = builder.bottomRightCorner;
	}

	/**
	 * Gets the top left corner component.
	 * @return returns the top left corner
	 */
	public String getTopLeftCorner() {
		return topLeftCorner;
	}

	/**
	 * Gets the top component pattern. This may be several characters long.
	 * 
	 * @return returns the top pattern
	 */
	public String getTopBorder() {
		return topBorder;
	}

	/**
	 * Gets the top right corner component.
	 * @return returns the top right corner
	 */
	public String getTopRightCorner() {
		return topRightCorner;
	}

	/**
	 * Gets the left border component. This should be a single character or 
	 * an empty string (although not strictly required).
	 * 
	 * @return returns the left border
	 */
	public String getLeftBorder() {
		return leftBorder;
	}

	/**
	 * Gets the right border component. This should be a single character or 
	 * an empty string (although not strictly required).
	 * 
	 * @return returns the right border
	 */
	public String getRightBorder() {
		return rightBorder;
	}

	/**
	 * Gets the bottom left corner component.
	 * @return returns the bottom left corner
	 */
	public String getBottomLeftCorner() {
		return bottomLeftCorner;
	}

	/**
	 * Gets the bottom component pattern. This may be several characters long.
	 * @return returns the bottom pattern
	 */
	public String getBottomBorder() {
		return bottomBorder;
	}

	/**
	 * Gets the bottom right corner component.
	 * @return returns the bottom right corner
	 */
	public String getBottomRightCorner() {
		return bottomRightCorner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bottomBorder == null) ? 0 : bottomBorder.hashCode());
		result = prime
				* result
				+ ((bottomLeftCorner == null) ? 0 : bottomLeftCorner.hashCode());
		result = prime
				* result
				+ ((bottomRightCorner == null) ? 0 : bottomRightCorner
						.hashCode());
		result = prime * result
				+ ((leftBorder == null) ? 0 : leftBorder.hashCode());
		result = prime * result
				+ ((rightBorder == null) ? 0 : rightBorder.hashCode());
		result = prime * result
				+ ((topBorder == null) ? 0 : topBorder.hashCode());
		result = prime * result
				+ ((topLeftCorner == null) ? 0 : topLeftCorner.hashCode());
		result = prime * result
				+ ((topRightCorner == null) ? 0 : topRightCorner.hashCode());
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
		TextBorderStyle other = (TextBorderStyle) obj;
		if (bottomBorder == null) {
			if (other.bottomBorder != null) {
				return false;
			}
		} else if (!bottomBorder.equals(other.bottomBorder)) {
			return false;
		}
		if (bottomLeftCorner == null) {
			if (other.bottomLeftCorner != null) {
				return false;
			}
		} else if (!bottomLeftCorner.equals(other.bottomLeftCorner)) {
			return false;
		}
		if (bottomRightCorner == null) {
			if (other.bottomRightCorner != null) {
				return false;
			}
		} else if (!bottomRightCorner.equals(other.bottomRightCorner)) {
			return false;
		}
		if (leftBorder == null) {
			if (other.leftBorder != null) {
				return false;
			}
		} else if (!leftBorder.equals(other.leftBorder)) {
			return false;
		}
		if (rightBorder == null) {
			if (other.rightBorder != null) {
				return false;
			}
		} else if (!rightBorder.equals(other.rightBorder)) {
			return false;
		}
		if (topBorder == null) {
			if (other.topBorder != null) {
				return false;
			}
		} else if (!topBorder.equals(other.topBorder)) {
			return false;
		}
		if (topLeftCorner == null) {
			if (other.topLeftCorner != null) {
				return false;
			}
		} else if (!topLeftCorner.equals(other.topLeftCorner)) {
			return false;
		}
		if (topRightCorner == null) {
			if (other.topRightCorner != null) {
				return false;
			}
		} else if (!topRightCorner.equals(other.topRightCorner)) {
			return false;
		}
		return true;
	}

}
