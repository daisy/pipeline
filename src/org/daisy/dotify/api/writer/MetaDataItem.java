package org.daisy.dotify.api.writer;

import javax.xml.namespace.QName;

public class MetaDataItem {

	private final QName key;
	private final String value;

	public MetaDataItem(QName key, String value) {
		this.key = key;
		this.value = value;
	}

	public QName getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
