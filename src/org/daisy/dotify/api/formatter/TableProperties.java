package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.translator.Border;

/**
 * Provides properties needed for a table. This class is immutable.
 * @author Joel Håkansson
 */
public final class TableProperties {
	private final int tableRowSpacing, tableColSpacing;
	private final int preferredEmptySpace;
	private final Float rowSpacing;
	private final BlockSpacing margin;
	private final BlockSpacing padding;
	private final Border border;
	
	/**
	 * Provides a builder for creating table properties
	 */
	public static class Builder {
		private int tableRowSpacing = 0;
		private int tableColSpacing = 0;
		private int preferredEmptySpace = 2;
		private Float rowSpacing = null;
		private BlockSpacing margin = new BlockSpacing.Builder().build();
		private BlockSpacing padding = new BlockSpacing.Builder().build();
		Border border = null;

		/**
		 * Creates a new builder
		 */
		public Builder() { }
		
		/**
		 * Sets the table row spacing
		 * @param value the table row spacing
		 * @return this builder
		 * @throws IllegalArgumentException if the value is less than zero
		 */
		public Builder tableRowSpacing(int value) {
			if (value<0) {
				throw new IllegalArgumentException("Negative values not allowed.");
			}
			this.tableRowSpacing = value;
			return this;
		}
		
		/**
		 * Sets the table column spacing
		 * @param value the table column spacing
		 * @return this builder
		 * @throws IllegalArgumentException if the value is less than zero
		 */
		public Builder tableColSpacing(int value) {
			if (value<0) {
				throw new IllegalArgumentException("Negative values not allowed.");
			}
			this.tableColSpacing = value;
			return this;
		}
		
		/**
		 * Sets the preferred empty space of the table when the text content of cells are shorter
		 * than the largest possible column width
		 * @param value the number of character positions to preferably leave empty
		 * @return returns this builder
		 */
		public Builder preferredEmptySpace(int value) {
			if (value<0) {
				throw new IllegalArgumentException("Negative values not allowed.");
			}
			this.preferredEmptySpace = value;
			return this;
		}
		
		/**
		 * Sets the row spacing for the resulting rows of text,
		 * that the table is made up of 
		 * @param value the row spacing
		 * @return returns this object
		 */
		public Builder rowSpacing(float value) {
			this.rowSpacing = value;
			return this;
		}
		
		/**
		 * Sets the margin
		 * @param value the margin
		 * @return returns this object
		 */
		public Builder margin(BlockSpacing value) {
			this.margin = value;
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
		 * Sets the border
		 * @param value the border
		 * @return returns this object
		 */
		public Builder border(Border value) {
			this.border = value;
			return this;
		}

		/**
		 * Creates new table properties based on the current
		 * state of this builder
		 * @return returns new table properties
		 */
		public TableProperties build() {
			return new TableProperties(this);
		}
	}

	private TableProperties(Builder builder) {
		this.tableRowSpacing = builder.tableRowSpacing;
		this.tableColSpacing = builder.tableColSpacing;
		this.preferredEmptySpace = builder.preferredEmptySpace;
		this.rowSpacing = builder.rowSpacing;
		this.margin = builder.margin;
		this.padding = builder.padding;
		this.border = builder.border;
	}

	/**
	 * Gets the table row spacing, in other words the vertical spacing
	 * between two rows of table cells.
	 * @return the table row spacing
	 */
	public int getTableRowSpacing() {
		return tableRowSpacing;
	}

	/**
	 * Gets the table column spacing, in other words the horizontal spacing
	 * between two columns of table cells.
	 * @return the table column spacing
	 */
	public int getTableColSpacing() {
		return tableColSpacing;
	}
	
	/**
	 * Gets the preferred empty space in table cells when the text content is 
	 * shorter than the available maximum column width
	 * @return returns the number of character positions to leave empty
	 */
	public int getPreferredEmtpySpace() {
		return preferredEmptySpace;
	}
	
	/**
	 * Gets the row spacing, or null if not set. This is the spacing
	 * between the output rows that are used to present the table.
	 * @return returns the row spacing
	 */
	public Float getRowSpacing() {
		return rowSpacing;
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
	 * Gets the border, or null if not set
	 * @return returns the border
	 */
	public Border getBorder() {
		return border;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((margin == null) ? 0 : margin.hashCode());
		result = prime * result + ((padding == null) ? 0 : padding.hashCode());
		result = prime * result + ((rowSpacing == null) ? 0 : rowSpacing.hashCode());
		result = prime * result + tableColSpacing;
		result = prime * result + tableRowSpacing;
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
		TableProperties other = (TableProperties) obj;
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
		if (rowSpacing == null) {
			if (other.rowSpacing != null) {
				return false;
			}
		} else if (!rowSpacing.equals(other.rowSpacing)) {
			return false;
		}
		if (tableColSpacing != other.tableColSpacing) {
			return false;
		}
		if (tableRowSpacing != other.tableRowSpacing) {
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
