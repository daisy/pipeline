package org.daisy.pipeline.script;

/**
 * Script port description.
 *
 * Note that a port in the {@link Script} API is more general then a port in XProc. In XProc, a port
 * can only accept/produce XML documents. In the {@link Script} API a port can accept/produce any
 * document.
 */
public interface ScriptPort {

	/**
	 * The name.
	 */
	public String getName();

	/**
	 * Whether it is a primary port.
	 */
	public boolean isPrimary();

	/**
	 * Whether the port can accept/produce a sequence of documents.
	 */
	public boolean isSequence();

	/**
	 * Whether a connection is required on the port.
	 *
	 * Output ports never require a connection.
	 */
	public boolean isRequired();

	/**
	 * The nice name.
	 */
	public String getNiceName();

	/**
	 * The description.
	 */
	public String getDescription();

	/**
	 * The media type.
	 */
	public String getMediaType();

	/**
	 * Get the file name extension for the given media type.
	 */
	public static String getFileExtension(String mediaType) {
		if (mediaType != null) {
			if ("application/epub+zip".equals(mediaType))
				return ".epub";
			if ("application/oebps-package+xml".equals(mediaType))
				return ".opf";
			if ("application/pdf".equals(mediaType))
				return ".pdf";
			if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mediaType))
				return ".docx";
			if ("application/vnd.pipeline.report+xml".equals(mediaType))
				return ".html";
			if ("application/xhtml+xml".equals(mediaType))
				return ".xhtml";
			if ("application/xml".equals(mediaType))
				return ".xml";
			if ("application/x-dtbook+xml".equals(mediaType))
				return ".xml";
			if ("application/x-obfl+xml".equals(mediaType))
				return ".xml";
			if ("application/x-pef+xml".equals(mediaType))
				return ".pef";
			if ("application/x-tex".equals(mediaType)
			    || "text/x-latex".equals(mediaType)
			    || "text/latex".equals(mediaType)
			    || "text/x-tex".equals(mediaType)
			    || "text/tex".equals(mediaType)
			    || "application/tex".equals(mediaType)
			    || "application/x-latex".equals(mediaType)
			    || "application/latex".equals(mediaType))
				return ".tex";
			if ("application/z3998-auth+xml".equals(mediaType))
				return ".xml";
			if ("text/html".equals(mediaType))
				return ".html";
			if ("text/plain".equals(mediaType)
			    || "text".equals(mediaType))
				return ".txt";
		}
		return ".xml";
	}
}
