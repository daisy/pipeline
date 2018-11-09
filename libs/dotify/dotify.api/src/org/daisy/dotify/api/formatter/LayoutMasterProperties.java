package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.translator.TextBorderStyle;

/**
 * Provides properties for a LayoutMaster.
 * 
 * @author Joel Håkansson
 */
public class LayoutMasterProperties {
	private final int flowWidth;
	private final int pageWidth;
	private final int pageHeight;
	private final int innerMargin;
	private final int outerMargin;
	private final float rowSpacing;
	private final boolean duplex;
	private final TextBorderStyle border;

	/**
	 * Configuration class for a ConfigurableLayoutMaster
	 * @author Joel Håkansson
	 *
	 */
	public static class Builder {
		private final int pageWidth;
		private final int pageHeight;

		// optional
		private int innerMargin = 0;
		private int outerMargin = 0;
		private float rowSpacing = 1;
		private boolean duplex = true;
		private TextBorderStyle border;

		/**
		 * Creates a new builder for layout master properties with the specified
		 * width and height.
		 * @param pageWidth the page width, in characters
		 * @param pageHeight the page height, in characters
		 */
		public Builder(int pageWidth, int pageHeight) {
			this.pageWidth = pageWidth;
			this.pageHeight = pageHeight;
			border = null;
		}

		/**
		 * Sets the inner margin, that is to say the margin closest
		 * to the binding edge of the page.
		 * @param value the inner margin, in characters
		 * @return returns this builder
		 */
		public Builder innerMargin(int value) {
			this.innerMargin = value;
			return this;
		}
		
		/**
		 * Sets the outer margin, that is to say the margin opposite
		 * to the binding edge of the page.
		 * @param value the outer margin, in characters
		 * @return returns this builder
		 */
		public Builder outerMargin(int value) {
			this.outerMargin = value;
			return this;
		}
		
		/**
		 * Sets the row spacing, that is to say the distance between two
		 * consecutive rows of text.
		 * @param value the row spacing, in character heights 
		 * @return returns this builder
		 * @throws IllegalArgumentException if the value is &lt;1
		 */
		public Builder rowSpacing(float value) {
			if (value < 1) {
				throw new IllegalArgumentException("Can't set  rowSpacing < 1: " + value);
			} else {
				this.rowSpacing = value;
			}
			return this;
		}
		
		/**
		 * Sets the duplex property, in other words whether to use
		 * both sides of a sheet or not. If set to true, both sides
		 * of a sheet should be used. If set to false, only one
		 * side of a sheet should be used.
		 * @param value the value
		 * @return returns this builder
		 */
		public Builder duplex(boolean value) {
			this.duplex = value;
			return this;
		}
		
		/**
		 * Sets a border surrounding the entire page.
		 * @param border the border
		 * @return returns this builder
		 */
		public Builder border(TextBorderStyle border) {
			this.border = border;
			return this;
		}
		
		/**
		 * Creates a new LayoutMasterProperties based on the current
		 * state of the builder.
		 * @return returns a new instance
		 */
		public LayoutMasterProperties build() {
			return new LayoutMasterProperties(this);
		}
	}
	
	private LayoutMasterProperties(Builder config) {
		int fsize = 0;
		if (config.border != null) {
			fsize = config.border.getLeftBorder().length() + config.border.getRightBorder().length();
		}
		this.flowWidth = config.pageWidth - config.innerMargin - config.outerMargin - fsize;
		this.pageWidth = config.pageWidth;
		this.pageHeight = config.pageHeight;
		this.innerMargin = config.innerMargin;
		this.outerMargin = config.outerMargin;
		this.rowSpacing = config.rowSpacing;
		this.duplex = config.duplex;
		this.border = config.border;
	}

	/**
	 * Gets the page width, in characters.
	 * @return returns the page width
	 */
	public int getPageWidth() {
		return pageWidth;
	}

	/**
	 * Gets the page height, in characters.
	 * @return returns the page height
	 */
	public int getPageHeight() {
		return pageHeight;
	}

	/**
	 * Gets the width available for the text body.
	 * @return returns the width, in characters
	 */
	public int getFlowWidth() {
		return flowWidth;
	}

	/**
	 * Gets the width of the inner margin, in characters.
	 * The inner margin is the margin closes to the binding.
	 * @return returns the width of the inner margin
	 */
	public int getInnerMargin() {
		return innerMargin;
	}

	/**
	 * Gets the width of the outer margin, in characters.
	 * The outer margin is the margin farthest from the binding.
	 * @return returns the width of the outer margin
	 */
	public int getOuterMargin() {
		return outerMargin;
	}
	
	/**
	 * Gets the row spacing, in character heights. A value
	 * of 1 indicates regular single line spacing. 2 indicates
	 * double line spacing.
	 * @return the row spacing
	 */
	public float getRowSpacing() {
		return rowSpacing;
	}
	
	/**
	 * Gets the duplex value. If true, both sides of a sheet
	 * should be used. If false, only one side of a sheet
	 * should be used.
	 * @return returns true if both sides of a sheet should be used,
	 * false otherwise
	 */
	public boolean duplex() {
		return duplex;
	}

	/**
	 * Gets the page border to use
	 * @return returns the page border
	 */
	public TextBorderStyle getBorder() {
		return border;
	}
}