package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.clients.JobConfigurationStorage;
import org.daisy.pipeline.clients.RequestLog;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VolatileWebserviceStorage   implements WebserviceStorage{
	
	private static final Logger logger = LoggerFactory
			.getLogger(VolatileWebserviceStorage.class);
	private ClientStorage clientStore;
	private RequestLog requestLog;
	private JobConfigurationStorage jobCnfStorage;

	public VolatileWebserviceStorage() {
		clientStore= new VolatileClientStorage();
		requestLog= new VolatileRequestLog();
		jobCnfStorage= new VolatileJobConfigurationStorage();
		logger.debug("Bringing VolatileWebserviceStorage up");
	}

	@Override
	public ClientStorage getClientStorage() {
		return clientStore;
	}

	@Override
	public RequestLog getRequestLog() {
		return requestLog;
	}

	@Override
	public JobConfigurationStorage getJobConfigurationStorage() {
		return this.jobCnfStorage;
	}
}