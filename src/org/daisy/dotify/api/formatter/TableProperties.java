package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.translator.TextBorderStyle;

/**
 * Provides properties needed for a table. This class is immutable.
 * @author Joel Håkansson
 */
public final class TableProperties {
	private final int rowSpacing, colSpacing;
	private final BlockSpacing margin;
	private final BlockSpacing padding;
	private final TextBorderStyle textBorderStyle;
	
	/**
	 * Provides a builder for creating table properties
	 */
	public static class Builder {
		private int rowSpacing = 0;
		private int colSpacing = 0;
		private BlockSpacing margin = new BlockSpacing.Builder().build();
		private BlockSpacing padding = new BlockSpacing.Builder().build();
		TextBorderStyle textBorderStyle = null;

		public Builder() { }
		
		/**
		 * Sets the row spacing
		 * @param value the row spacing
		 * @return this builder
		 * @throws IllegalArgumentException if the value is less than zero
		 */
		public Builder rowSpacing(int value) {
			if (value<0) {
				throw new IllegalArgumentException("Negative values not allowed.");
			}
			this.rowSpacing = value;
			return this;
		}
		
		/**
		 * Sets the column spacing
		 * @param value the column spacing
		 * @return this builder
		 * @throws IllegalArgumentException if the value is less than zero
		 */
		public Builder colSpacing(int value) {
			if (value<0) {
				throw new IllegalArgumentException("Negative values not allowed.");
			}
			this.colSpacing = value;
			return this;
		}
		
		/**
		 * Sets the margin
		 * @return returns this object
		 */
		public Builder margin(BlockSpacing value) {
			this.margin = value;
			return this;
		}
		
		/**
		 * Sets the padding
		 * @return returns this object
		 */
		public Builder padding(BlockSpacing value) {
			this.padding = value;
			return this;
		}
		
		/**
		 * Sets the text border style
		 * @param value the text border style
		 * @return returns this object
		 */
		public Builder textBorderStyle(TextBorderStyle value) {
			this.textBorderStyle = value;
			return this;
		}

		public TableProperties build() {
			return new TableProperties(this);
		}
	}

	private TableProperties(Builder builder) {
		this.rowSpacing = builder.rowSpacing;
		this.colSpacing = builder.colSpacing;
		this.margin = builder.margin;
		this.padding = builder.padding;
		this.textBorderStyle = builder.textBorderStyle;
	}

	public int getRowSpacing() {
		return rowSpacing;
	}

	public int getColSpacing() {
		return colSpacing;
	}
	
	/**
	 * Gets the margin
	 * @return returns the margin
	 */
	public BlockSpacing getMargin() {
		return margin;
	}
	
	/**
	 * Gets the padding
	 * @return returns the padding
	 */
	public BlockSpacing getPadding() {
		return padding;
	}
	
	/**
	 * Gets the text border style, or null if not set
	 * @return returns the text border style
	 */
	public TextBorderStyle getTextBorderStyle() {
		return textBorderStyle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + colSpacing;
		result = prime * result + ((margin == null) ? 0 : margin.hashCode());
		result = prime * result + ((padding == null) ? 0 : padding.hashCode());
		result = prime * result + rowSpacing;
		result = prime * result + ((textBorderStyle == null) ? 0 : textBorderStyle.hashCode());
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
		TableProperties other = (TableProperties) obj;
		if (colSpacing != other.colSpacing) {
			return false;
		}
		if (margin == null) {
			if (other.margin != null) {
				return false;
			}
		} else if (!margin.equals(other.margin)) {
			return false;
		}
		if (padding == null) {
			if (other.padding != null) {
				return false;
			}
		} else if (!padding.equals(other.padding)) {
			return false;
		}
		if (rowSpacing != other.rowSpacing) {
			return false;
		}
		if (textBorderStyle == null) {
			if (other.textBorderStyle != null) {
				return false;
			}
		} else if (!textBorderStyle.equals(other.textBorderStyle)) {
			return false;
		}
		return true;
	}

}
