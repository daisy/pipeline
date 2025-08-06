package org.daisy.pipeline.job;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.script.BoundScript;

import com.google.common.base.Optional;

/**
 * The interface JobManager offers a simple way of managing jobs.
 */
public interface JobManager extends JobFactory {

	/**
	 * Creates a job builder for the given script with bound inputs and outputs. Upon creating the
	 * job, it will be added to the storage and the execution queue.
	 */
	@Override
	public JobBuilder newJob(BoundScript boundScript);

	/**
	 * Gets the jobs.
	 *
	 * @return the jobs
	 */
	public Iterable<Job> getJobs();

	/**
	 * Deletes a job.
	 *
	 * @param id the id
	 * @return the job
	 */
	public Optional<Job> deleteJob(JobId id);

	/**
	 * Deletes all jobs.
	 *
	 */
	public Iterable<Job> deleteAll();

	/**
	 * Get the job with the given job ID
	 */
	public Optional<Job> getJob(JobId id);

	/**
	 * Get the single job that matches the given (partial) job ID.
	 *
	 * Don't return any jobs if multiple jobs match the argument.
	 *
	 * @param prefix The job ID of the returned job must start with this string
	 */
	public Optional<Job> findJob(String prefix);

	public JobQueue getExecutionQueue();

	public interface JobBuilder extends JobFactory.JobBuilder {
		@Override
		public JobBuilder withNiceName(String niceName);
		public JobBuilder withPriority(Priority priority);
		public JobBuilder withBatchId(JobBatchId id);
		/**
		 * @throws UnsupportedOperationException Jobs managed by a {@link JobManager} are never
		 *                                       automatically closed.
		 */
		@Override
		public JobBuilder closeOnExit() throws UnsupportedOperationException;
	}
}
