package org.daisy.pipeline.persistence.impl.job;

import java.io.File;
import java.net.URI;

import javax.xml.namespace.QName;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobUUIDGenerator;
import org.daisy.pipeline.persistence.impl.Database;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentOptionResultTest {

	Database db;
	PersistentOptionResult pi1;
	String name = "result";
	File file = Mocks.result1;
	URI path = URI.create("file.xml");
	JobResult result;
	JobId id1;

	@Before
	public void setUp(){
		db = DatabaseProvider.getDatabase();
		id1 = new JobUUIDGenerator().generateId();
		pi1 =new PersistentOptionResult(id1, new JobResult(path, file, null) {}, new QName(name));
		db.addObject(pi1);
	}

	@After
	public void tearDown(){
		db.deleteObject(pi1);
	}

	@Test
	public void testPersistOptionResult() throws Exception{
		PersistentOptionResult stored = db.getEntityManager().find(PersistentOptionResult.class,new PersistentOptionResult.PK(id1, path.toString()));
		Assert.assertEquals(name,stored.getOptionName().toString());
		Assert.assertEquals(path.toString(), stored.getIdx());
		Assert.assertEquals(file, stored.getPath());
	}
}
