package org.daisy.pipeline.job;

import java.io.File;

import junit.framework.Assert;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobContextFactory;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.daisy.pipeline.script.BoundXProcScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
@RunWith(MockitoJUnitRunner.class)
public class JobContextFactoryTest   {
        @Mock Client client;
        @Mock JobContext mCtxt;

        @Mock BoundXProcScript boundScript;

	String oldIoBase="";
	File tmpdir;
        
        @Before
        public void setUp(){
		oldIoBase=System.getProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE);	
		tmpdir= new File(System.getProperty("java.io.tmpdir"));
		System.setProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE,tmpdir.toString());	
        }
        @After
        public void tearDown(){
		if(oldIoBase!=null)
			System.setProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE,oldIoBase);	
        }
        @Test
        public void mappingContext(){
                String name="nice name";
                
                JobContextFactory factory=Mockito.spy(new JobContextFactory(client));
                Mockito.doReturn(mCtxt).when(factory).newJobContext(true,name,null,boundScript,null);
                
                JobContext ctxt = factory.newMappingJobContext(name,null,boundScript,null);
                
                Mockito.verify(factory,Mockito.times(1)).newJobContext(true,name,null,boundScript,null);
                Assert.assertEquals(mCtxt, ctxt);

        }

        @Test
        public void nonMappingContext(){
                String name="nice name";
                JobContextFactory factory=Mockito.spy(new JobContextFactory(client));
                Mockito.doReturn(mCtxt).when(factory).newJobContext(false,name,null,boundScript,null);

                JobContext ctxt=factory.newJobContext(name,null,boundScript);
                
                Mockito.verify(factory,Mockito.times(1)).newJobContext(false,name,null,boundScript,null);
                Assert.assertEquals(mCtxt, ctxt);

        }
}
