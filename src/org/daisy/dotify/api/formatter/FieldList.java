package org.daisy.dotify.api.formatter;

import java.util.List;

/**
 * Provides a list of fields, typically used with headers and footers.
 * 
 * @author Joel Håkansson
 */
public interface FieldList {
	
	/**
	 * Gets the row-spacing following this field list.
	 * @return returns the spacing, or null if not defined
	 */
	public Float getRowSpacing();

	/**
	 * Gets the list of fields.
	 * @return returns the list of fields
	 */
	public List<Field> getFields();
}
