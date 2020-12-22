package org.daisy.pipeline.clients;

public interface WebserviceStorage {
	/**
	 * @return the clientStore
	 */
	public ClientStorage getClientStorage();

	/**
	 * @return the requestLog
	 */
	public RequestLog getRequestLog();

	/**
	 * Returns a job configuration storage 
	 * object
	 * @return the job storage
	 */
	public JobConfigurationStorage getJobConfigurationStorage();
	
}



