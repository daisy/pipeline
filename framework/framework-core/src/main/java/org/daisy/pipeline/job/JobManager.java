package org.daisy.pipeline.job;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.script.BoundXProcScript;

import com.google.common.base.Optional;

/**
 * The Interface JobManager offers a simple way of managing jobs.
 */
public interface JobManager {

	/**
	 * Creates a job for the given script
	 */
	public JobBuilder newJob(BoundXProcScript boundScript);

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
	 * Gets the job.
	 *
	 * @param id the id
	 * @return the job
	 */
	public Optional<Job> getJob(JobId id);

	public JobQueue getExecutionQueue();

	public interface JobBuilder {
		public JobBuilder isMapping(boolean mapping);
		public JobBuilder withResources(JobResources resources);
		public JobBuilder withNiceName(String niceName);
		public JobBuilder withPriority(Priority priority);
		public JobBuilder withBatchId(JobBatchId id);
		public Optional<Job> build();
	}
}
