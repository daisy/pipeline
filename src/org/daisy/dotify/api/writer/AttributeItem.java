package org.daisy.dotify.api.writer;

public class AttributeItem {
	private final String name;
	private final String value;
	
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
