package org.daisy.pipeline.persistence.impl.job;

import java.net.URI;

import org.daisy.pipeline.job.Index;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobUUIDGenerator;
import org.daisy.pipeline.persistence.impl.Database;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentPortResultTest {
	Database db;
	PersistentPortResult pi1;
	String name="result";
	URI path=URI.create("file:/tmp/file.xml");
	String idx="file.xml";
	JobId id1;
	JobResult result;
	@Before	
	public void setUp(){
		result= new JobResult.Builder().withPath(path).withIdx(idx).build();
		db=DatabaseProvider.getDatabase();
		id1= new JobUUIDGenerator().generateId();

		pi1=new PersistentPortResult(id1,result,name);
		db.addObject(pi1);
	}	

	@After
	public void tearDown(){
		db.deleteObject(pi1);
	}	

	@Test
	public void portName() throws Exception{
		PersistentPortResult stored=db.getEntityManager().find(PersistentPortResult.class,new PersistentPortResult.PK(id1,new Index(idx)));
		Assert.assertEquals(name,stored.getPortName());
		
	}

	@Test
	public void result() throws Exception{
		PersistentPortResult stored=db.getEntityManager().find(PersistentPortResult.class,new PersistentPortResult.PK(id1,new Index(idx)));
		Assert.assertEquals(result,stored.getJobResult());
		
	}
	
}
