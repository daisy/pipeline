package org.daisy.common.xproc;

import org.daisy.common.messaging.MessageAppender;

/**
 * Observer object to monitor a pipeline execution
 */
public interface XProcMonitor {

	/**
	 * {@link MessageAppender} to which the pipeline should append messages.
	 */
	public MessageAppender getMessageAppender();

}
