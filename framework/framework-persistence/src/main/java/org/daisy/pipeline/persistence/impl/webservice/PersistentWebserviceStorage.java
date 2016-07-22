package org.daisy.pipeline.persistence.impl.webservice;

import javax.persistence.EntityManagerFactory;

import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.webserviceutils.requestlog.RequestLog;
import org.daisy.pipeline.webserviceutils.storage.JobConfigurationStorage;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentWebserviceStorage implements WebserviceStorage {
	private static final Logger logger = LoggerFactory
			.getLogger(PersistentWebserviceStorage.class);
	private ClientStorage clientStore;
	private RequestLog requestLog;
	private JobConfigurationStorage jobCnfStorage;
	private Database database;

	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.database = new Database(emf);
	}

	public void activate() {
		logger.debug("Bringing WebserviceStorage up");
		this.clientStore = new PersistentClientStorage(this.database);
		this.requestLog = new PersistentRequestLog(this.database);
		this.jobCnfStorage=new PersistentJobConfigurationStorage(this.database);
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
