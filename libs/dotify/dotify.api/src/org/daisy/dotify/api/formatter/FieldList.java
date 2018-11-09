package org.daisy.dotify.api.formatter;

import java.util.List;

/**
 * Provides a list of fields, typically used with headers and footers.
 * 
 * @author Joel Håkansson
 */
public class FieldList {
	private final List<Field> contents;
	private final Float rowSpacing;
	
	/**
	 * Provides a field list builder
	 */
	public static class Builder {
		private final List<Field> contents;
		//Optional
		private Float rowSpacing = null;
		
		/**
		 * Creates a new builder with the specified fields
		 * @param contents the fields in this builder
		 */
		public Builder(List<Field> contents) {
			this.contents = contents;
		}
		
		/**
		 * Sets the row spacing for this field list
		 * @param rowSpacing the row spacing
		 * @return returns this object
		 */
		public Builder rowSpacing(Float rowSpacing) {
			this.rowSpacing = rowSpacing;
			return this;
		}

		/**
		 * Builds a new field list with the current configuration
		 * @return returns a new field list
		 */
		public FieldList build() {
			return new FieldList(this);
		}
	}
	
	private FieldList(Builder builder) {
		this.contents = builder.contents;
		this.rowSpacing = builder.rowSpacing;
	}
	
	/**
	 * Gets the row-spacing following this field list.
	 * @return returns the spacing, or null if not defined
	 */
	public Float getRowSpacing() {
		return rowSpacing;
	}

	/**
	 * Gets the list of fields.
	 * @return returns the list of fields
	 */
	public List<Field> getFields() {
		return contents;
	}

}