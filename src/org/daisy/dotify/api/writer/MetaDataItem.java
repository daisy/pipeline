package org.daisy.dotify.api.writer;

import javax.xml.namespace.QName;

public class MetaDataItem {

	private final QName key;
	private final String value;
	private final AttributeItem attribute;
	
	public static class Builder {
		private final QName elementKey;
		private final String elementValue;
		private AttributeItem attribute;
		
		public Builder(QName elementKey, String elementValue) {
			this.elementKey = elementKey;
			this.elementValue = elementValue;
		}
		
		public Builder attribute(AttributeItem value) {
			this.attribute = value;
			return this;
		}
		
		public MetaDataItem build() {
			return new MetaDataItem(this);
		}
	}

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

	public QName getKey() {
		return key;
	}

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
