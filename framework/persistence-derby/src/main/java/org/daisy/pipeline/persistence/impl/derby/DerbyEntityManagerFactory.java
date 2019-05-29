package org.daisy.pipeline.persistence.impl.derby;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

import org.daisy.common.properties.PropertyPublisher;
import org.daisy.common.properties.PropertyPublisherFactory;
import org.daisy.pipeline.persistence.ForwardingEntityManagerFactory;
import org.daisy.pipeline.properties.Properties;

import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "daisy-derby-emf",
	immediate = true,
	service = { EntityManagerFactory.class },
	property = { "osgi.unit.name:String=pipeline-pu" }
)
public class DerbyEntityManagerFactory extends  ForwardingEntityManagerFactory{

	private static final String DERBY_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DERBY_DB_URL = "jdbc:derby:"+Properties.getProperty("org.daisy.pipeline.data")+ File.separator + "db;create=true";
	
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
	
	@Reference(
		name = "EntityManagerFactoryBuilder",
		unbind = "-",
		service = EntityManagerFactoryBuilder.class,
		target = "(osgi.unit.name=pipeline-pu)",
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setBuilder(EntityManagerFactoryBuilder builder){
		setEntityManagerFactory(builder.createEntityManagerFactory(props));
	}
	@Reference(
		name = "PropertyPublisherFactory",
		unbind = "unsetPropertyPublisherFactory",
		service = PropertyPublisherFactory.class,
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC
	)
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
	@Activate
	public void init() {
		logger.debug("initialize the EMF");
		createEntityManager();
	}
	
}
