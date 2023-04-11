package org.daisy.pipeline.script;

/**
 * Metadata associated with a port of an XProc step through custom <code>px:*</code> attributes.
 */
public class XProcPortMetadata {

	/**
	 * The media type to annotate status ports with.
	 *
	 * See <a href="https://github.com/daisy/pipeline/wiki/StatusXML"
	 * >https://github.com/daisy/pipeline/wiki/StatusXML</a>.
	 */
	public static final String MEDIA_TYPE_STATUS_XML = "application/vnd.pipeline.status+xml";

	final private String niceName;
	final private String description;
	final private String mediaType;

	/**
	 * Instantiates a new {@link XProcPortMetadata}.
	 *
	 * @param niceName
	 *            the nice name
	 * @param description
	 *            the description
	 * @param mediaType
	 *            the media type
	 */
	public XProcPortMetadata(String niceName, String description, String mediaType) {
		super();
		this.niceName = niceName;
		this.description = description;
		this.mediaType = mediaType;
	}

	public String getNiceName() {
		return niceName;
	}

	public String getDescription() {
		return description;
	}

	public String getMediaType() {
		return mediaType;
	}
}
