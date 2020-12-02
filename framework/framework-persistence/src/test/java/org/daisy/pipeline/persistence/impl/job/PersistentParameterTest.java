package org.daisy.pipeline.persistence.impl.job;

import javax.xml.namespace.QName;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobUUIDGenerator;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.PersistentParameter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentParameterTest   {
	
	Database db;
	JobId id1;
	String port1="params";
	String value="some value";
	QName qn1=new QName("http://daisy.com","test");
	PersistentParameter pi1;
	@Before	
	public void setUp(){
		db=DatabaseProvider.getDatabase();
		id1= new JobUUIDGenerator().generateId();
		qn1=new QName("http://daisy.com","test");
		pi1=new PersistentParameter(id1,port1,qn1,value);
		db.addObject(pi1);
	}	

	@After
	public void tearDown(){
		db.deleteObject(pi1);
	}	
	@Test
	public void simpleParameter(){
		PersistentParameter stored=db.getEntityManager().find(PersistentParameter.class,new PersistentParameter.PK(id1,port1,qn1));
		Assert.assertNotNull(stored);
		Assert.assertEquals(stored.getJobId(),id1);
		Assert.assertEquals(stored.getPort(),port1);
		Assert.assertEquals(stored.getName(),qn1);
		Assert.assertEquals(stored.getValue(),value);
	}
}
