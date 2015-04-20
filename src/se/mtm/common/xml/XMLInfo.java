package se.mtm.common.xml;

import org.xml.sax.Attributes;

public class XMLInfo {
	private final String uri;
	private final String localName;
	private final String qName;
	private final Attributes attributes;
	
	public XMLInfo(String uri, String localName, String qName, Attributes attributes) {
		super();
		this.uri = uri;
		this.localName = localName;
		this.qName = qName;
		this.attributes = attributes;
	}

	public String getUri() {
		return uri;
	}

	public String getLocalName() {
		return localName;
	}

	public String getqName() {
		return qName;
	}

	public Attributes getAttributes() {
		return attributes;
	}

}
