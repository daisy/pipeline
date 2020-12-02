package org.daisy.pipeline.job;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;

import com.google.common.base.Optional;

public interface JobStorage  extends Iterable<AbstractJob> {
	/** It is crucial to update the reference to the job returned by this method as 
	 * the object itself maybe changed by the implementation
	 */
	public Optional<AbstractJob> add(Priority priority, AbstractJobContext ctxt);
	public Optional<AbstractJob> remove(JobId jobId);
	public Optional<AbstractJob> get(JobId id);
        public JobStorage filterBy(Client client);
        public JobStorage filterBy(JobBatchId id);
}
