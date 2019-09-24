package org.daisy.pipeline.job.impl;
import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobContextFactory;
import org.daisy.pipeline.job.JobExecutionService;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager.JobBuilder;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobStorage;
import org.daisy.pipeline.script.BoundXProcScript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJobManagerTest{
        @Mock JobStorage storage;
        @Mock MessageStorage messageStorage;
        @Mock JobExecutionService service; 
        @Mock JobContextFactory factory;
        @Mock Client client;
        @Mock BoundXProcScript script;
        @Mock JobContext ctxt;
        @Mock Optional<Job> job;
        @Mock JobResources resources;


        DefaultJobManager jobManager;
        @Before
        public void setUp(){
                jobManager=Mockito.spy(new DefaultJobManager(storage,messageStorage,service,factory));
        }

        @Test
        public void builderOptions(){
                JobBuilder builder = Mockito.spy(jobManager.newJob(script));
                Mockito.when(
                        factory.newJobContext(
                                Mockito.anyBoolean(),
                                Mockito.anyString(),
                                Mockito.nullable(JobBatchId.class),
                                Mockito.any(BoundXProcScript.class),
                                Mockito.nullable(JobResources.class)))
                        .thenReturn(ctxt);
                Mockito.when(job.isPresent()).thenReturn(false);
                Mockito.when(
                        storage.add(
                                Mockito.any(Priority.class),
                                Mockito.any(JobContext.class)))
                        .thenReturn(job);

                //by default
                builder.build();
                Mockito.verify(factory,Mockito.times(1)).newJobContext(false,"",null,script,null);
                Mockito.verify(jobManager,Mockito.times(1)).newJob(Priority.MEDIUM,ctxt);

                //priority
                builder= Mockito.spy(jobManager.newJob(script).withPriority(Priority.HIGH));
                builder.build();
                Mockito.verify(factory,Mockito.times(2)).newJobContext(false,"",null,script,null);
                Mockito.verify(jobManager,Mockito.times(1)).newJob(Priority.HIGH,ctxt);
                //mapping
                builder= Mockito.spy(jobManager.newJob(script).isMapping(true));
                builder.build();
                Mockito.verify(factory,Mockito.times(1)).newJobContext(true,"",null,script,null);
                //nice name
                builder= Mockito.spy(jobManager.newJob(script).withNiceName("my name"));
                builder.build();
                Mockito.verify(factory,Mockito.times(1)).newJobContext(false,"my name",null,script,null);
                //Resource collection
                builder= Mockito.spy(jobManager.newJob(script).withResources(this.resources));
                builder.build();
                Mockito.verify(factory,Mockito.times(1)).newJobContext(false,"",null,script,this.resources);
                JobBatchId id=JobIdFactory.newBatchId();
                builder= Mockito.spy(jobManager.newJob(script).withBatchId(id));
                builder.build();
                Mockito.verify(factory,Mockito.times(1)).newJobContext(false,"",null,script,this.resources);
        }

}
