package org.daisy.dotify.api.translator;


public class TextBorderStyle {
	private final String topLeftCorner, topBorder, topRightCorner, leftBorder,
			rightBorder, bottomLeftCorner, bottomBorder, bottomRightCorner;

	/**
	 * Loaded on first access
	 */
	public final static TextBorderStyle NONE = new Builder().build();

	/*
	 * enum StylePreset {
	 * NONE, SOLID_OUTER
	 * }
	 */

	public static class Builder {
		String topLeftCorner, topBorder, topRightCorner, leftBorder,
				rightBorder, bottomLeftCorner, bottomBorder, bottomRightCorner;

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

	public String getTopLeftCorner() {
		return topLeftCorner;
	}

	public String getTopBorder() {
		return topBorder;
	}

	public String getTopRightCorner() {
		return topRightCorner;
	}

	public String getLeftBorder() {
		return leftBorder;
	}

	public String getRightBorder() {
		return rightBorder;
	}

	public String getBottomLeftCorner() {
		return bottomLeftCorner;
	}

	public String getBottomBorder() {
		return bottomBorder;
	}

	public String getBottomRightCorner() {
		return bottomRightCorner;
	}

}
