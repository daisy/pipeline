package org.daisy.pipeline.job;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.event.MessageStorage;

import com.google.common.base.Optional;

public interface JobStorage extends Iterable<AbstractJob> {

	/**
	 * It is crucial to update the reference to the job returned by this method as
	 * the object itself may be changed by the implementation
	 */
	public Optional<AbstractJob> add(AbstractJob job);
	/**
	 * Should also remove the messages associated with this job from the message storage.
	 */
	public Optional<AbstractJob> remove(JobId jobId);
	public Optional<AbstractJob> get(JobId id);
	public JobStorage filterBy(Client client);
	public JobStorage filterBy(JobBatchId id);

	public MessageStorage getMessageStorage();

}
