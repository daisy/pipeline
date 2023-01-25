package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.clients.JobConfigurationStorage;

/** in dbless mode no configuration is stored */
public class VolatileJobConfigurationStorage implements JobConfigurationStorage {

	@Override
	public boolean add(JobId id, String configuration) {
		return true;
	}

	@Override
	public String get(JobId id) {
		return "";
	}

	@Override
	public boolean delete(JobId id) {
		return true;
	}
}
