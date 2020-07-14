package org.daisy.pipeline.persistence.impl.derby;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.daisy.pipeline.persistence.ForwardingEntityManagerFactory;
import org.daisy.pipeline.properties.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "daisy-derby-emf",
	service = { EntityManagerFactory.class },
	property = { "osgi.unit.name:String=pipeline-pu" }
)
public class DerbyEntityManagerFactory extends ForwardingEntityManagerFactory {

	private static final String DERBY_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DERBY_DB_URL = "jdbc:derby:"+Properties.getProperty("org.daisy.pipeline.data")+"/db;create=true";
	
	protected static Logger logger = LoggerFactory
			.getLogger(DerbyEntityManagerFactory.class.getName());
	
	private static final Map<String,Object> props = new HashMap<String, Object>();
	static {
		props.put(JAVAX_PERSISTENCE_JDBC_DRIVER,
				DERBY_JDBC_DRIVER);
		props.put(JAVAX_PERSISTENCE_JDBC_URL,
				DERBY_DB_URL);
		logger.debug(DERBY_DB_URL);
	}

	public DerbyEntityManagerFactory(){
		super(props);
	}

	@Activate
	public void init() {
		logger.debug("initialize the EMF");
		createEntityManager();
	}
	
}
