

package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.TypedQuery;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.JobBuilder;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.ClientFilter;
import org.daisy.pipeline.persistence.impl.job.PersistentJob;
import org.daisy.pipeline.persistence.impl.job.PersistentJobContext;
import org.daisy.pipeline.persistence.impl.job.QueryDecorator;
import org.daisy.pipeline.persistence.impl.job.PersistentJob.PersistentJobBuilder;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientFilterTest{
        Job job; 
        Job job2; 
        Database db;
        PersistentClient client;

        @Before
        public void setUp(){
		db=DatabaseProvider.getDatabase();

		System.setProperty("org.daisy.pipeline.iobase",System.getProperty("java.io.tmpdir"));
		PersistentJobContext.setScriptRegistry(new Mocks.DummyScriptService(Mocks.buildScript()));
		JobBuilder builder= new PersistentJobBuilder(db).withContext(Mocks.buildContext());
		job =(PersistentJob) builder.build();//new PersistentJob(Job.newJob(Mocks.buildContext()),db);
		builder= new PersistentJobBuilder(db).withContext(Mocks.buildContext());
		job2 =(PersistentJob) builder.build();//new PersistentJob(Job.newJob(Mocks.buildContext()),db);
                client=new PersistentClient("cli","sadfsa",Role.ADMIN,"asdf",Priority.LOW);

        }
	@After
	public void tearDown(){
		db.deleteObject(job);
		db.deleteObject(job.getContext().getClient());
		db.deleteObject(job2);
		db.deleteObject(job2.getContext().getClient());
        }


        @Test
        public void getEmpty(){
                QueryDecorator<PersistentJob> dec=new ClientFilter(db.getEntityManager(),client);
                TypedQuery<PersistentJob> q=dec.getQuery(PersistentJob.class); 
                Assert.assertEquals("No jobs should be found",0,q.getResultList().size());
        }

        @Test
        public void getByClient(){
                QueryDecorator<PersistentJob> dec=new ClientFilter(db.getEntityManager(),job.getContext().getClient());
                TypedQuery<PersistentJob> q=dec.getQuery(PersistentJob.class); 
                Assert.assertEquals("One job should match",1,q.getResultList().size());
                Assert.assertEquals("And it should be this one",job.getId().toString(),((Job)q.getSingleResult()).getId().toString());
        }


        
}
