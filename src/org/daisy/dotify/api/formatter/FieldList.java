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
	
	public static class Builder {
		private final List<Field> contents;
		//Optional
		private Float rowSpacing = null;
		
		public Builder(List<Field> contents) {
			this.contents = contents;
		}
		
		public Builder rowSpacing(Float rowSpacing) {
			this.rowSpacing = rowSpacing;
			return this;
		}
		
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