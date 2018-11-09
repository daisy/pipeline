package org.daisy.dotify.api.writer;

/**
 * Provides an attribute key/value combination for a meta data item.
 * @author Joel Håkansson
 *
 */
public class AttributeItem {
	private final String name;
	private final String value;
	
	/**
	 * Creates a new attribute item with the supplied parameters.
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public AttributeItem(String name, String value) {
		if (name==null || value==null) {
			throw new IllegalArgumentException("Null value not allowed.");
		}
		this.name = name;
		this.value = value;
	}

	/**
	 * Gets the attribute name
	 * @return returns the name, never null
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the attribute value
	 * @return returns the attribute value, never null
	 */
	public String getValue() {
		return value;
	}

}
