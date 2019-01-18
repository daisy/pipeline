/*
 *
 */
package org.daisy.pipeline.script;

// TODO: Auto-generated Javadoc
/**
 * Metadata associated to a port.
 */
public class XProcPortMetadata {

	/**
	 * The media type to annotate status ports with.
	 *
	 * See https://github.com/daisy/pipeline/wiki/StatusXML.
	 */
	public static final String MEDIA_TYPE_STATUS_XML = "application/vnd.pipeline.status+xml";

	/**
	 * Builds the {@link XProcPortMetadata} object
	 */
	public static final class Builder {

		/** The nice name. */
		private String niceName;

		/** The description. */
		private String description;

		/** The media type. */
		private String mediaType;
		/** If it is required **/
		private boolean required;

		/** If it is required **/
		private boolean primary=true;
		/**
		 * With nice name.
		 * 
		 * @param niceName
		 *            the nice name
		 * @return the builder
		 */
		public Builder withNiceName(String niceName) {
			this.niceName = niceName;
			return this;
		}

		/**
		 * Sets the port as required (no default connections where requried):w
		 * 
		 * @return the builder
		 */
		public Builder withRequired(boolean required) {
			this.required = required;
			return this;
		}

		/**
		 * With description.
		 * 
		 * @param description
		 *            the description
		 * @return the builder
		 */
		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		/**
		 * With media type.
		 * 
		 * @param mediaType
		 *            the media type
		 * @return the builder
		 */
		public Builder withMediaType(String mediaType) {
			this.mediaType = mediaType;
			return this;
		}

		/**
		 *  Set the port as primary.
		 * 
		 * @param mediaType
		 *            the media type
		 * @return the builder
		 */
		public Builder withPrimary(boolean primary) {
			this.primary= primary;
			return this;
		}
		/**
		 * Builds the instance.
		 * 
		 * @return the x proc port metadata
		 */
		public XProcPortMetadata build() {
			return new XProcPortMetadata(niceName, description, mediaType,
					required,primary);
		}

	}

	/** The nice name. */
	final private String niceName;

	/** The description. */
	final private String description;

	/** The media type. */
	final private String mediaType;

	/** required port */
	private boolean required;
	/** primary port*/
	private boolean primary;

	/**
	 * Instantiates a new x proc port metadata.
	 * 
	 * @param niceName
	 *            the nice name
	 * @param description
	 *            the description
	 * @param mediaType
	 *            the media type
	 */
	public XProcPortMetadata(String niceName, String description,
			String mediaType, boolean required,boolean primary) {
		super();
		this.niceName = niceName;
		this.description = description;
		this.mediaType = mediaType;
		this.required = required;
                this.primary=primary;
	}

	/**
	 * Gets the nice name.
	 * 
	 * @return the nice name
	 */
	public String getNiceName() {
		return niceName;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the media type.
	 * 
	 * @return the media type
	 */
	public String getMediaType() {
		return mediaType;
	}

	/**
	 * The port is required if no default connection was available connection
	 * (p:data,p:empty,p:inline,p:document)
	 * 
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
         * The port is primary
	 * 
	 * @return the required
	 */

	public boolean isPrimary() {
		return primary;
	}

}
