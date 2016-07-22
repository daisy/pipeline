/*
 *
 */
package org.daisy.pipeline.webserviceutils.callback;

import org.daisy.pipeline.job.JobId;

public interface CallbackRegistry {

	public Iterable<Callback> getCallbacks(JobId id);
	public void addCallback(Callback callback);
	public void removeCallback(Callback callback);
}
