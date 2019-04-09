package org.daisy.pipeline.nonpersistent.impl.webservice;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.webserviceutils.requestlog.RequestLog;
import org.daisy.pipeline.webserviceutils.storage.JobConfigurationStorage;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "volatile-webservice-storage",
	immediate = true,
	service = { WebserviceStorage.class }
)
public class VolatileWebserviceStorage   implements WebserviceStorage{
	
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

	@Activate
	public void activate() {
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

