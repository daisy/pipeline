package org.daisy.pipeline.persistence.impl.webservice;

import javax.persistence.EntityManagerFactory;

import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.webserviceutils.requestlog.RequestLog;
import org.daisy.pipeline.webserviceutils.storage.JobConfigurationStorage;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "webservice-storage",
	immediate = true,
	service = { WebserviceStorage.class }
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

	@Activate
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
