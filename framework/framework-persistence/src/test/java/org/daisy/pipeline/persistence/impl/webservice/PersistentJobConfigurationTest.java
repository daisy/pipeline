package org.daisy.pipeline.persistence.impl.webservice;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.DatabaseProvider;
import org.daisy.pipeline.persistence.impl.webservice.PersistentJobConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentJobConfigurationTest  {
	JobId id;
	String configuration;
	Database db;
	PersistentJobConfiguration jobCnf;

	@Before
	public void setUp(){
		configuration="ASDFASDFASDFASDFASDF";
		id=JobIdFactory.newId();
		db=DatabaseProvider.getDatabase();
		jobCnf= new PersistentJobConfiguration.Builder().withId(id).withConfiguration(configuration).build();
		db.addObject(jobCnf);

	}
	
	@Test
	public void store(){
		PersistentJobConfiguration pcnf=db.getEntityManager().find(PersistentJobConfiguration.class,this.id.toString());
		Assert.assertEquals(id,pcnf.getId());
		Assert.assertEquals(configuration,pcnf.getConfiguration());
	}

}
