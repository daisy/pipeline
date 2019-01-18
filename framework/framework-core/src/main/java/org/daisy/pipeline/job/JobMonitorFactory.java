package org.daisy.pipeline.job;

public interface JobMonitorFactory {
	/**
	 * @param live Whether the job needs to be monitorable while it runs, or whether the messages
	 *             can be assumed to be fixed.
	 */
	public JobMonitor newJobMonitor(JobId id, boolean live);
}
