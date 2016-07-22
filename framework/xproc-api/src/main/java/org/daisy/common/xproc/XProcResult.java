package org.daisy.common.xproc;



// TODO: Auto-generated Javadoc
/**
 * The Interface XProcResult gives access to the result of executing the pipeline and the message produced during the process.
 */
public interface XProcResult {

	/**
	 * Writes the output.
	 *
	 * @param output the output
	 */
	void writeTo(XProcOutput output);

	/**
	 * Gets the messages produced during the pipeline execution.
	 *
	 * @return the messages
	 */
	//MessageAccessor getMessages();
}
