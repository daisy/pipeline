package org.daisy.pipeline.persistence.impl.job;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.persistence.EntityManager;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobUUIDGenerator;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.PersistentInputPort;
import org.daisy.pipeline.persistence.impl.job.PersistentSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentInputPortTest   {

	Database db;
	PersistentInputPort pi1;
	String name1="source";
	JobId id1;
	@Before	
	public void setUp(){
		System.setProperty("org.daisy.pipeline.iobase",System.getProperty("java.io.tmpdir"));
		db=DatabaseProvider.getDatabase();
		id1= new JobUUIDGenerator().generateId();
		pi1=new PersistentInputPort(id1,name1);
		db.addObject(pi1);
	}	

	@After
	public void tearDown(){
		db.deleteObject(pi1);
	}	
	@Test
	public void simpleInput(){
		PersistentInputPort stored=db.getEntityManager().find(PersistentInputPort.class,new PersistentInputPort.PK(id1,name1));
		Assert.assertNotNull(stored);
		Assert.assertEquals(stored.getId().getJobId(),id1);
		Assert.assertEquals(stored.getId().getName(),name1);
	}
	@Test
	public void inputWithSources(){
		PersistentInputPort stored=db.getEntityManager().find(PersistentInputPort.class,new PersistentInputPort.PK(id1,name1));
		String src1="file:/tmp/input1.xml";
		String src2="file:/tmp/input2.xml";
		String src3="file:/tmp/input3.xml";
		stored.addSource(new PersistentSource(src1));
		stored.addSource(new PersistentSource(src2));
		stored.addSource(new PersistentSource(src3));

		
		db.updateObject(stored);
		stored=db.getEntityManager().find(PersistentInputPort.class,new PersistentInputPort.PK(id1,name1));
		Collection<PersistentSource> srcs=stored.getSources();
		HashSet<String> sources=new HashSet<String>();
		
		for( PersistentSource src:srcs ){
			sources.add(src.getSystemId());
		}
		Assert.assertEquals(sources.size(),3);
		Assert.assertTrue(sources.containsAll(Arrays.asList(new String[]{src1,src2,src3})));
	}
}
