package org.daisy.pipeline.webserviceutils.storage;

import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.webserviceutils.requestlog.RequestLog;

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



