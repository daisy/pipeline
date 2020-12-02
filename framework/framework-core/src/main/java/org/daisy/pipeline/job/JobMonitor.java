package org.daisy.pipeline.job;

import java.util.function.Consumer;

import org.daisy.common.messaging.MessageAccessor;

public interface JobMonitor {

	/**
	 * Job messages may be accessed through this {@link MessageAccessor}.
	 */
	public MessageAccessor getMessageAccessor();

	/**
	 * Get notified when the job status changes.
	 */
	public StatusNotifier getStatusUpdates();

}
