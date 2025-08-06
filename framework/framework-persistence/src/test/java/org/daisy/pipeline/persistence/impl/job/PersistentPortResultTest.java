package org.daisy.pipeline.persistence.impl.job;

import java.io.File;
import java.net.URI;

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
	File file = Mocks.result1;
	URI path = URI.create("file.xml");
	JobId id1;

	@Before
	public void setUp(){
		db = DatabaseProvider.getDatabase();
		id1 = new JobUUIDGenerator().generateId();
		pi1 = new PersistentPortResult(id1, new JobResult(path, file, null) {}, name);
		db.addObject(pi1);
	}

	@After
	public void tearDown(){
		db.deleteObject(pi1);
	}

	@Test
	public void testPersistPortResult() throws Exception{
		PersistentPortResult stored = db.getEntityManager().find(PersistentPortResult.class,new PersistentPortResult.PK(id1, path.toString()));
		Assert.assertEquals(name,stored.getPortName());
		Assert.assertEquals(path.toString(), stored.getIdx());
		Assert.assertEquals(file, stored.getPath());
	}
}
