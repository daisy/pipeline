package org.daisy.pipeline.persistence.impl.derby;

import java.util.HashMap;
import java.util.Map;

import org.daisy.common.properties.PropertyPublisher;
import org.daisy.common.properties.PropertyPublisherFactory;
import org.daisy.pipeline.persistence.ForwardingEntityManagerFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerbyEntityManagerFactory extends  ForwardingEntityManagerFactory{

	private static final String DERBY_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DERBY_DB_URL = "jdbc:derby:"+System.getProperty("org.daisy.pipeline.data")+"/db;create=true";
	
	protected static Logger logger = LoggerFactory
			.getLogger(DerbyEntityManagerFactory.class.getName());
	
	private static  final Map<String,Object> props=new HashMap<String, Object>();
	static{

		props.put(JAVAX_PERSISTENCE_JDBC_DRIVER,
				DERBY_JDBC_DRIVER);
		props.put(JAVAX_PERSISTENCE_JDBC_URL,
				DERBY_DB_URL);
		logger.debug(DERBY_DB_URL);
	}
	
	public void setBuilder(EntityManagerFactoryBuilder builder){
		setEntityManagerFactory(builder.createEntityManagerFactory(props));
	}
	public void setPropertyPublisherFactory(PropertyPublisherFactory propertyPublisherFactory){
		PropertyPublisher propertyPublisher=propertyPublisherFactory.newPropertyPublisher();	
		//the property publishing step goes here
		propertyPublisher.publish(JAVAX_PERSISTENCE_JDBC_DRIVER, DERBY_JDBC_DRIVER,this.getClass());
		propertyPublisher.publish(JAVAX_PERSISTENCE_JDBC_URL, DERBY_DB_URL,this.getClass());

	}

	public void unsetPropertyPublisherFactory(PropertyPublisherFactory propertyPublisherFactory){
		PropertyPublisher propertyPublisher=propertyPublisherFactory.newPropertyPublisher();	
		//the property unpublishing step goes here
		propertyPublisher.unpublish(JAVAX_PERSISTENCE_JDBC_DRIVER ,  this.getClass());
		propertyPublisher.unpublish(JAVAX_PERSISTENCE_JDBC_URL    ,  this.getClass());

	}
	public void init() {
		logger.debug("initialize the EMF");
		createEntityManager();
	}
	
}
