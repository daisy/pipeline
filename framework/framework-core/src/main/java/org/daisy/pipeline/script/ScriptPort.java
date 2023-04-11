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

}
