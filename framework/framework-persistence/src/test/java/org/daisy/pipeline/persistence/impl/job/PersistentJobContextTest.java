package org.daisy.pipeline.persistence.impl.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentJobContextTest  {

	ScriptRegistry scriptRegistry;
	Database db;
	PersistentJobContext ctxt;
	JobId id;
	JobBatchId batchId;
	URI logFile;
	File tempDir;
	@Before	
	public void setUp(){
		tempDir = Files.createTempDir();
		scriptRegistry = new Mocks.DummyScriptService(Mocks.buildScript());
		ctxt=new PersistentJobContext(Mocks.buildContext(null, JobIdFactory.newBatchId(), tempDir), null);
		logFile=ctxt.getLogFile();
		id=ctxt.getId();
		batchId=ctxt.getBatchId();
                System.out.println("batchID "+batchId);
		db=DatabaseProvider.getDatabase();
		db.addObject(ctxt);
	}
	@After
	public void tearDown() {
		try {
			db.deleteObject(ctxt);
			db.deleteObject(ctxt.getClient());
		} finally {
			if (tempDir != null)
				try {
					FileUtils.deleteDirectory(tempDir);
				} catch (IOException e) {
				}
		}
	}

        @Test
        public void getClientPriority(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
                Assert.assertEquals("Check priority is low",Priority.LOW,jCtxt.getClient().getPriority());

        }

	@Test
	public void storeInput(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		jCtxt.finalize(scriptRegistry, null);
		Assert.assertEquals(jCtxt.getId(),id);
		Assert.assertEquals(jCtxt.getScript().getId(), Mocks.scriptId);
		Assert.assertEquals(jCtxt.getLogFile(),this.logFile);
	}
	@Test
	public void inputPortsTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		ScriptInput inputs = jCtxt.getInput();
		HashSet<String> expectedSrcs=new HashSet<String>();
		for (Source psrc : inputs.getInput("source")) {
			expectedSrcs.add(psrc.getSystemId());
		}
		Assert.assertTrue(expectedSrcs.contains(Mocks.file1));
		Assert.assertTrue(expectedSrcs.contains(Mocks.file2));
	}


	@Test
	public void optionTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		ScriptInput inputs = jCtxt.getInput();
		Assert.assertEquals(inputs.getOption(Mocks.opt2Name), Lists.newArrayList(Mocks.value1));
	}

	@Test
	public void mapperTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class, id.toString());
		Assert.assertEquals(jCtxt.getResultMapper(), new URIMapper(tempDir.toURI(), Mocks.out));
	}
	
	@Test
	public void resultPortTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		Assert.assertEquals(jCtxt.getResults().getResults(Mocks.portResult),
		                    ctxt.getResults().getResults(Mocks.portResult));
	}

	@Test
	public void batchIdTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		Assert.assertEquals(jCtxt.getBatchId().toString(),batchId.toString());
	}

}
