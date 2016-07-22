package org.daisy.pipeline.webserviceutils.storage;

import org.daisy.pipeline.job.JobId;

public interface JobConfigurationStorage  {
	/**
	 * Stores a new job configuration
	 *
	 * @param id Job's id
	 * @param configuration the job configuration string representation
	 */
	public boolean add(JobId id,String configuration);	

	/**
	 * Returns the configuration for 
	 * a single job
	 * @param id
	 * @return the String represenetation of the configuration
	 */
	public String get(JobId id);

	/**
	 * Deletes the job's configuration from this storage 
	 *
	 * @param id
	 * @return
	 */
	public boolean delete(JobId id);
}
