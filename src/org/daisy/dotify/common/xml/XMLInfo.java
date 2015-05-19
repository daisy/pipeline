package org.daisy.dotify.common.xml;

import org.xml.sax.Attributes;

public class XMLInfo {
	private final String uri;
	private final String localName;
	private final String qName;
	private final Attributes attributes;
	private final String publicId;
	private final String systemId;
	
	public static class Builder {
		private String uri = null;
		private String localName = null;
		private String qName = null;
		private Attributes attributes = null;
		private String publicId = null;
		private String systemId = null;
		
		public Builder() { }
		
		public Builder uri(String value) {
			this.uri = value;
			return this;
		}
		
		public Builder localName(String value) {
			this.localName = value;
			return this;
		}
		
		public Builder qName(String value) {
			this.qName = value;
			return this;
		}
		
		public Builder attributes(Attributes value) {
			this.attributes = value;
			return this;
		}
		
		public Builder publicId(String value) {
			this.publicId = value;
			return this;
		}
		
		public Builder systemId(String value) {
			this.systemId = value;
			return this;
		}
		
		public XMLInfo build() {
			return new XMLInfo(this);
		}
	}
	
	private XMLInfo(Builder builder) {
		this.uri = builder.uri;
		this.localName = builder.localName;
		this.qName = builder.qName;
		this.attributes = builder.attributes;
		this.publicId = builder.publicId;
		this.systemId = builder.systemId;
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

	public String getPublicId() {
		return publicId;
	}

	public String getSystemId() {
		return systemId;
	}

}
