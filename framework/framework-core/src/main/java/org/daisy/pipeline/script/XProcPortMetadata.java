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
	final private boolean reusable;

	// FIXME: delete all constructors except for the last one
	// (which is the only one actually used, StaxXProcScriptParser)

	public XProcPortMetadata(String niceName, String description, String mediaType) {
		this(niceName, description, mediaType, false);
	}

	public XProcPortMetadata(String niceName, String description, String mediaType, boolean reusable) {
		super();
		this.niceName = niceName;
		this.description = description;
		this.mediaType = mediaType;
		this.reusable = reusable;
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

	/**
	 * Whether inputs are suitable for being remembered by user interfaces, for reuse
	 * in future jobs.
	 */
	public boolean isReusable() {
		return reusable;
	}
}
