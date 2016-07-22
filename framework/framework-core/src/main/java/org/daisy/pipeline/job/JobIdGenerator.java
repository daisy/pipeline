package org.daisy.pipeline.job;


// TODO: Auto-generated Javadoc
/**
 * The Interface JobIdGenerator .
 */
public interface JobIdGenerator {

	/**
	 * Generate id.
	 * @return the job id
	 */
	public JobId generateId();

	/**
	 * Generate id from string.
	 *
	 * @param base the base
	 * @return the job id
	 */
	public JobId generateIdFromString(String base);
}
