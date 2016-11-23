package org.daisy.dotify.api.writer;

import javax.xml.namespace.QName;

/**
 * Provides a simple meta data item. A meta data item can contain an element name,
 * text content and an attribute key/value. Thereby, this model supports the two
 * most common types of meta data elements in XML-formats while remaining reasonably 
 * well-defined:
 * <ul>
 * <li>Specific element with content, for example:
 * <pre>
 * &lt;dc:title&gt;Example&lt;/dc:title&gt;
 * </pre>
 * </li>
 * <li>Generic element + specific attribute key and value, for example:
 * <pre>
 * &lt;meta name="dc:title" value="Example"/&gt;
 * </pre>
 * </li>
 * </ul>
 *
 * @author Joel Håkansson
 *
 */
public class MetaDataItem {

	private final QName key;
	private final String value;
	private final AttributeItem attribute;
	
	
	/**
	 * Provides a meta data item builder.
	 */
	public static class Builder {
		private final QName elementKey;
		private final String elementValue;
		private AttributeItem attribute;

		/**
		 * Creates a new meta data item builder
		 * @param elementKey the element name
		 * @param elementValue the element value, (in other words, its text contents)
		 *
		 */
		public Builder(QName elementKey, String elementValue) {
			this.elementKey = elementKey;
			this.elementValue = elementValue;
		}
		
		/**
		 * Sets the item's attribute.
		 * @param value the attribute
		 * @return returns this builder
		 */
		public Builder attribute(AttributeItem value) {
			this.attribute = value;
			return this;
		}
		
		/**
		 * Creates a new meta data item based on the current
		 * state of the builder.
		 * @return returns a new meta data item
		 */
		public MetaDataItem build() {
			return new MetaDataItem(this);
		}
	}

	/**
	 * Creates a new meta data item with the specified parameters.
	 * @param key the element name
	 * @param value the text contents of the element
	 */
	public MetaDataItem(QName key, String value) {
		this.key = key;
		this.value = value;
		this.attribute = null;
	}
	
	private MetaDataItem(Builder builder) {
		this.key = builder.elementKey;
		this.value = builder.elementValue;
		this.attribute = builder.attribute;
	}

	/**
	 * Gets the element name
	 * @return returns the name of the meta data item
	 */
	public QName getKey() {
		return key;
	}

	/**
	 * Gets the text contents of this meta data item
	 * @return returns the text contents
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the attribute, if specified or null
	 * @return the attribute
	 */
	public AttributeItem getAttribute() {
		return attribute;
	}

}
