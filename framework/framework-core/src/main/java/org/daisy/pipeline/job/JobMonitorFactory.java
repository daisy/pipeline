package org.daisy.pipeline.job;

import org.daisy.common.messaging.MessageAccessor;

public interface JobMonitorFactory {

	/**
	 * Get the monitor for a job.
	 *
	 * If a "live" monitor (for monitoring the job while it runs) is present in memory, always
	 * returns it. Otherwise loads the messages from storage.
	 */
	public JobMonitor newJobMonitor(JobId id);

	/**
	 * Create a "live" job monitor from a given MessageAccessor and StatusNotifier. To be used for
	 * newly created jobs.
	 */
	public JobMonitor newJobMonitor(JobId id, MessageAccessor messageAccessor, StatusNotifier statusNotifier);

}
