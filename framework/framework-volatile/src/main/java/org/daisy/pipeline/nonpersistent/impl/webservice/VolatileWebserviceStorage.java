package org.daisy.pipeline.nonpersistent.impl.webservice;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.clients.JobConfigurationStorage;
import org.daisy.pipeline.clients.RequestLog;
import org.daisy.pipeline.clients.WebserviceStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "volatile-webservice-storage",
	service = { WebserviceStorage.class }
)
public class VolatileWebserviceStorage   implements WebserviceStorage{
	
	private static final boolean VOLATILE_DISABLED = "true".equalsIgnoreCase(
		Properties.getProperty("org.daisy.pipeline.persistence"));
	private static final Logger logger = LoggerFactory
			.getLogger(VolatileWebserviceStorage.class);
	private ClientStorage clientStore;
	private RequestLog requestLog;
	private JobConfigurationStorage jobCnfStorage;

	public VolatileWebserviceStorage(){

		clientStore= new VolatileClientStorage();
		requestLog= new VolatileRequestLog();
		jobCnfStorage= new VolatileJobConfigurationStorage();
	}

	/**
	 * @throws RuntimeException if volatile storage is disabled through the org.daisy.pipeline.persistence system property.
	 */
	@Activate
	public void activate() {
		if (VOLATILE_DISABLED)
			throw new RuntimeException("Volatile storage is disabled");
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

