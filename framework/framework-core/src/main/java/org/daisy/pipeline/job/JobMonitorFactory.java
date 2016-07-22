package org.daisy.pipeline.job;

public interface JobMonitorFactory {
	public JobMonitor newJobMonitor(JobId id);
}
