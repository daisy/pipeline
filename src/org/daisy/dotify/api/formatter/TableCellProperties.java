package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.translator.Border;

/**
 * Provides properties needed for a table cell. This class is immutable.
 * @author Joel Håkansson
 */
public final class TableCellProperties {
	private final int rowSpan, colSpan;
	private final TextBlockProperties textBlockProperties; 
	private final BlockSpacing padding;
	private final Border border;
	
	/**
	 * Provides a builder for creating table properties
	 */
	public static class Builder {
		private int rowSpan = 1;
		private int colSpan = 1;
		private BlockSpacing padding = new BlockSpacing.Builder().build();
		private TextBlockProperties textBlockProps = new TextBlockProperties.Builder().build();
		private Border border = null;

		/**
		 * Creates a new builder
		 */
		public Builder() { }
		
		/**
		 * Sets the row span
		 * @param value the value
		 * @return returns this object
		 */
		public Builder rowSpan(int value) {
			this.rowSpan = value;
			return this;
		}
		
		/**
		 * Sets the column span
		 * @param value the value
		 * @return returns this object
		 */
		public Builder colSpan(int value) {
			this.colSpan = value;
			return this;
		}

		/**
		 * Sets the padding
		 * @param value the padding
		 * @return returns this object
		 */
		public Builder padding(BlockSpacing value) {
			this.padding = value;
			return this;
		}
		
		/**
		 * Gets the text block properties builder
		 * @param value the text block properties
		 * @return returns the text block properties builder
		 */
		public Builder textBlockProperties(TextBlockProperties value) {
			this.textBlockProps = value;
			return this;
		}
		
		/**
		 * Sets the border
		 * @param value the border
		 * @return returns this object
		 */
		public Builder border(Border value) {
			this.border = value;
			return this;
		}

		/**
		 * Creates a new table cell properties using the current
		 * state of this builder.
		 * @return returns a new table cell properties
		 */
		public TableCellProperties build() {
			return new TableCellProperties(this);
		}
	}

	private TableCellProperties(Builder builder) {
		this.rowSpan = builder.rowSpan;
		this.colSpan = builder.colSpan;
		this.padding = builder.padding;
		this.textBlockProperties = builder.textBlockProps;
		this.border = builder.border;
	}
	
	/**
	 * Gets the row span, in other words, the number of rows that
	 * this cell spans.
	 * 
	 * @return returns the row span 
	 */
	public int getRowSpan() {
		return rowSpan;
	}


	/**
	 * Gets the column span, in other words, the number of columns that
	 * this cell spans.
	 * 
	 * @return returns the row span 
	 */
	public int getColSpan() {
		return colSpan;
	}

	/**
	 * Gets the padding
	 * @return returns the padding
	 */
	public BlockSpacing getPadding() {
		return padding;
	}
	
	/**
	 * Gets the border, or null if not set
	 * @return returns the border
	 */
	public Border getBorder() {
		return border;
	}

	/**
	 * Gets the text block properties
	 * @return returns the text block properties
	 */
	public TextBlockProperties getTextBlockProperties() {
		return textBlockProperties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + colSpan;
		result = prime * result + ((padding == null) ? 0 : padding.hashCode());
		result = prime * result + rowSpan;
		result = prime * result + ((textBlockProperties == null) ? 0 : textBlockProperties.hashCode());
		result = prime * result + ((border == null) ? 0 : border.hashCode());
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
		TableCellProperties other = (TableCellProperties) obj;
		if (colSpan != other.colSpan) {
			return false;
		}
		if (padding == null) {
			if (other.padding != null) {
				return false;
			}
		} else if (!padding.equals(other.padding)) {
			return false;
		}
		if (rowSpan != other.rowSpan) {
			return false;
		}
		if (textBlockProperties == null) {
			if (other.textBlockProperties != null) {
				return false;
			}
		} else if (!textBlockProperties.equals(other.textBlockProperties)) {
			return false;
		}
		if (border == null) {
			if (other.border != null) {
				return false;
			}
		} else if (!border.equals(other.border)) {
			return false;
		}
		return true;
	}

}
