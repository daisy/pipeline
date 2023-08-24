package org.daisy.pipeline.persistence.impl.webservice;

import javax.persistence.EntityManagerFactory;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.clients.JobConfigurationStorage;
import org.daisy.pipeline.clients.RequestLog;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.persistence.impl.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "webservice-storage",
	service = {
		WebserviceStorage.class,
		PersistentWebserviceStorage.class
	}
)
public class PersistentWebserviceStorage implements WebserviceStorage {

	private static final Logger logger = LoggerFactory
			.getLogger(PersistentWebserviceStorage.class);
	private ClientStorage clientStore;
	private RequestLog requestLog;
	private JobConfigurationStorage jobCnfStorage;
	private Database database;

	@Reference(
		name = "entity-manager-factory",
		unbind = "-",
		service = EntityManagerFactory.class,
		target = "(osgi.unit.name=pipeline-pu)",
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.database = new Database(emf);
	}

	/**
	 * @throws RuntimeException if persistent storage is disabled through the org.daisy.pipeline.persistence system property.
	 */
	@Activate
	public void activate() {
		if ("false".equalsIgnoreCase(System.getProperty("org.daisy.pipeline.persistence"))) // property only used in tests
			throw new RuntimeException("Persistent storage is disabled");
		logger.debug("Bringing PersistentWebserviceStorage up");
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
