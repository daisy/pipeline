package org.daisy.pipeline.persistence.impl.mysql;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

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
	name = "daisy-mysql-emf",
	immediate = true,
	service = { EntityManagerFactory.class },
	property = { "osgi.unit.name:String=pipeline-pu" }
)
public class MySQLEntityManagerFactory extends  ForwardingEntityManagerFactory{

	private static final String ORG_DAISY_PERSISTENCE_PASSWORD = "org.daisy.pipeline.persistence.password";
	private static final String ORG_DAISY_PERSISTENCE_USER = "org.daisy.pipeline.persistence.user";
	private static final String ORG_DAISY_PERSISTENCE_URL = "org.daisy.pipeline.persistence.url";
	private static final String COM_MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
	protected static Logger logger = LoggerFactory
			.getLogger(MySQLEntityManagerFactory.class.getName());
	
	private static  final Map<String,Object> props=new HashMap<String, Object>();
	static {
		props.put(JAVAX_PERSISTENCE_JDBC_DRIVER,
				COM_MYSQL_JDBC_DRIVER);
		props.put(JAVAX_PERSISTENCE_JDBC_URL,
				Properties.getProperty(ORG_DAISY_PERSISTENCE_URL));
		props.put(JAVAX_PERSISTENCE_JDBC_USER,
				Properties.getProperty(ORG_DAISY_PERSISTENCE_USER));
		props.put(JAVAX_PERSISTENCE_JDBC_PASSWORD,
				Properties.getProperty(ORG_DAISY_PERSISTENCE_PASSWORD));
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
	@Activate
	public void init() {
		logger.debug("initialize the mysql EMF");
		createEntityManager();
	}
	
}
