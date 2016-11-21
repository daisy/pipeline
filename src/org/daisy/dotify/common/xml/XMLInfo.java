package org.daisy.dotify.common.xml;

import org.xml.sax.Attributes;

/**
 * Provides basic information about an XML-document.
 * 
 * @author Joel Håkansson
 */
public class XMLInfo {
	private final String uri;
	private final String localName;
	private final String qName;
	private final Attributes attributes;
	private final String publicId;
	private final String systemId;
	
	/**
	 * Provides a builder for XML info.
	 */
	public static class Builder {
		private String uri = null;
		private String localName = null;
		private String qName = null;
		private Attributes attributes = null;
		private String publicId = null;
		private String systemId = null;
		
		/**
		 * Creates a new empty builder.
		 */
		public Builder() {
			//no required parameters for this builder
		}

		/**
		 * Sets the uri.
	     * @param value The Namespace URI, or the empty string if the 
	     * 				element has no Namespace URI or if Namespace
	     * 				processing is not being performed.
		 * @return returns this builder
		 */
		public Builder uri(String value) {
			this.uri = value;
			return this;
		}
		
		/**
		 * Sets the local name.
		 * @param value	The local name (without prefix), or the empty string 
		 * 				if Namespace processing is not being performed.
		 * @return returns this builder
		 */
		public Builder localName(String value) {
			this.localName = value;
			return this;
		}
		
		/**
		 * Sets the qualified name.
		 * @param value	The qualified name (with prefix), or the 
		 * 				empty string if qualified names are not available.
		 * @return returns this builder
		 */
		public Builder qName(String value) {
			this.qName = value;
			return this;
		}
		
		/**
		 * Sets the attributes.
		 * @param value The attributes attached to the element.
		 * @return returns this builder
		 */
		public Builder attributes(Attributes value) {
			this.attributes = value;
			return this;
		}

		/**
		 * Sets the public identifier of the DTD declaration.
		 * @param value The public identifier, or null if none is available.
		 * @return returns this builder
		 */
		public Builder publicId(String value) {
			this.publicId = value;
			return this;
		}
		
		/**
		 * Sets the system identifier of the DTD declaration.
		 * @param value The system identifier provided in the XML document, or null if none is available.
		 * @return returns this builder
		 */
		public Builder systemId(String value) {
			this.systemId = value;
			return this;
		}
		
		/**
		 * Creates a new XMLInfo instance using the current state of the builder. 
		 * @return returns a new XMLInfo instance
		 */
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

	/**
	 * Gets the Namespace URI, or the empty string if the element has 
	 * no Namespace URI or if Namespace processing is not being performed.
	 * Or null, if the uri has not been set.
	 * @return returns the namespace uri, or null
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Gets the local name (without prefix), or the empty string if Namespace 
	 * processing is not being performed.
	 * Or null, if the local name has not been set.
	 * @return returns the local name, or null
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Gets the qualified name (with prefix), or the empty string if qualified 
	 * names are not available.
	 * Or null, if the qualified name has not been set.
	 * @return returns the qualified name, or null
	 */
	public String getqName() {
		return qName;
	}

	/**
	 * Gets the attributes attached to the element or null if the attributes has
	 * not been set.
	 * @return returns the attributes, or null
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	/**
	 * Gets the public identifier of the DTD declaration or null if the public
	 * identifier has not been set or if the document doesn't contain a public identifier.
	 * @return returns the public identifier, or null
	 */
	public String getPublicId() {
		return publicId;
	}

	/**
	 * Gets the system identifier provided in the XML document or null if the system
	 * identifier has not been set or if the document doesn't contain a system identifier.
	 * @return returns the system identifier, or null
	 */
	public String getSystemId() {
		return systemId;
	}

}
