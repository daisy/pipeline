package org.daisy.pipeline.job;

import java.util.function.Consumer;

public interface StatusNotifier {

	/**
	 * Get notified of changes in job status.
	 */
	public void listen(Consumer<Job.Status> listener);
	public void unlisten(Consumer<Job.Status> listener);

}
