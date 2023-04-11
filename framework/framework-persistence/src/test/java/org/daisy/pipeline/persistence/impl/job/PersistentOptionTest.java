package org.daisy.pipeline.persistence.impl.job;

import javax.xml.namespace.QName;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobUUIDGenerator;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.PersistentOption;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class PersistentOptionTest   {
	
	Database db;
	PersistentOption po1;
	String name1;
	Iterable<String> value;
	JobId id1;
	@Before	
	public void setUp(){
		db=DatabaseProvider.getDatabase();
		id1= new JobUUIDGenerator().generateId();
		name1 = "test";
		value = Lists.newArrayList("some value");
		po1 = new PersistentOption(id1, name1, value);
		db.addObject(po1);
	}	

	@After
	public void tearDown(){
		db.deleteObject(po1);
	}	
	@Test
	public void retrieveOption(){
		PersistentOption stored = db.getEntityManager().find(PersistentOption.class,
		                                                     new PersistentOption.PK(id1, new QName(name1)));
		Assert.assertNotNull(stored);
		Assert.assertEquals(stored.getJobId(),id1);
		Assert.assertEquals(stored.getName(), name1);
	}
}
