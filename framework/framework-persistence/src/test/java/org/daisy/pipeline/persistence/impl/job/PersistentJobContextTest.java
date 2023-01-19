package org.daisy.pipeline.persistence.impl.job;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcInput;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.script.ScriptRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;


public class PersistentJobContextTest  {

	ScriptRegistry scriptRegistry;
	Database db;
	PersistentJobContext ctxt;
	JobId id;
	JobBatchId batchId;
	URI logFile;
	@Before	
	public void setUp(){
		scriptRegistry = new Mocks.DummyScriptService(Mocks.buildScript());
		ctxt=new PersistentJobContext(Mocks.buildContext(null, JobIdFactory.newBatchId()), null);
		logFile=ctxt.getLogFile();
		id=ctxt.getId();
		batchId=ctxt.getBatchId();
                System.out.println("batchID "+batchId);
		db=DatabaseProvider.getDatabase();
		db.addObject(ctxt);
	}
	@After
	public void tearDown(){
		db.deleteObject(ctxt);
                db.deleteObject(ctxt.getClient());
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
		Assert.assertEquals(jCtxt.getScript().getDescriptor().getId(),Mocks.scriptId);
		Assert.assertEquals(jCtxt.getLogFile(),this.logFile);
	}
	@Test
	public void inputPortsTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		XProcInput inputs=jCtxt.getInputs();
		HashSet<String> expectedSrcs=new HashSet<String>();
		for ( Supplier<Source> psrc:inputs.getInputs("source")){
			expectedSrcs.add(psrc.get().getSystemId());	
		}
		Assert.assertTrue(expectedSrcs.contains(Mocks.file1));
		Assert.assertTrue(expectedSrcs.contains(Mocks.file2));
	}


	@Test
	public void optionTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		XProcInput inputs=jCtxt.getInputs();
		Assert.assertTrue(inputs.getOptions().containsKey(Mocks.opt1Qname));
		Assert.assertTrue(inputs.getOptions().containsKey(Mocks.opt2Qname));
		Assert.assertEquals(inputs.getOptions().get(Mocks.opt1Qname),Mocks.value1);
		Assert.assertEquals(inputs.getOptions().get(Mocks.opt2Qname),Mocks.value2);
	}

	@Test
	public void paramTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		XProcInput inputs=jCtxt.getInputs();
		Assert.assertEquals(inputs.getParameters(Mocks.paramPort).get(new QName(Mocks.qparam)),Mocks.paramVal);
	}

	@Test
	public void mapperTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		Assert.assertEquals(jCtxt.getResultMapper(), new URIMapper(Mocks.in, Mocks.out));
	}
	
	@Test
	public void resultPortTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		Assert.assertEquals(jCtxt.getResults().getResults(Mocks.portResult),
		                    ctxt.getResults().getResults(Mocks.portResult));
	}
	@Test
	public void resultOptionTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		Assert.assertEquals(jCtxt.getResults().getResults(Mocks.opt1Qname),
		                    ctxt.getResults().getResults(Mocks.opt1Qname));
	}
	@Test
	public void batchIdTest(){
		PersistentJobContext jCtxt= db.getEntityManager().find(PersistentJobContext.class,id.toString());
		Assert.assertEquals(jCtxt.getBatchId().toString(),batchId.toString());
	}

}
