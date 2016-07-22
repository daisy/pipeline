package org.daisy.pipeline.persistence.impl.job;

import java.util.HashSet;
import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.JobBuilder;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.PersistentJob;
import org.daisy.pipeline.persistence.impl.job.PersistentJobContext;
import org.daisy.pipeline.persistence.impl.job.PersistentJob.PersistentJobBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentJobTest   {

	Database db;
	JobId id;
	JobId idHigh;
	PersistentJob job;
	PersistentJob jobHigh;
	@Before	
	public void setUp(){
		//script setup
			
		db=DatabaseProvider.getDatabase();
		System.setProperty("org.daisy.pipeline.iobase",System.getProperty("java.io.tmpdir"));
		PersistentJobContext.setScriptRegistry(new Mocks.DummyScriptService(Mocks.buildScript()));
		JobBuilder builder= new PersistentJobBuilder(db).withContext(Mocks.buildContext());
		job =(PersistentJob) builder.build();//new PersistentJob(Job.newJob(Mocks.buildContext()),db);
		id=job.getContext().getId();
                //high priority
                builder= new PersistentJobBuilder(db).withContext(Mocks.buildContext());
		jobHigh =(PersistentJob) builder.withPriority(Priority.HIGH).build();//new PersistentJob(Job.newJob(Mocks.buildContext()),db);
		idHigh=jobHigh.getContext().getId();
	}
	@After
	public void tearDown(){
		db.deleteObject(job);
		db.deleteObject(job.getContext().getClient());
		db.deleteObject(jobHigh);
		db.deleteObject(jobHigh.getContext().getClient());
	}	

        @Test 
        public void storeTest(){
                PersistentJob pjob= db.getEntityManager().find(PersistentJob.class,id.toString());
                Assert.assertEquals(pjob.getContext().getId(),id);
                Assert.assertEquals("Default priority",Priority.MEDIUM,pjob.getPriority());

        }
        @Test 
        public void highPriority(){
                PersistentJob pjob= db.getEntityManager().find(PersistentJob.class,idHigh.toString());
                Assert.assertEquals("High priority",Priority.HIGH,pjob.getPriority());

        }
        @Test 
        public void changeStatusTest(){
                PersistentJob pjob= db.getEntityManager().find(PersistentJob.class,id.toString());
                pjob.setDatabase(db);
                pjob.setStatus(Job.Status.DONE);
                pjob.onStatusChanged(Job.Status.DONE);
                pjob= db.getEntityManager().find(PersistentJob.class,id.toString());
                Assert.assertEquals(Job.Status.DONE,pjob.getStatus());

        }
	@Test 
	public void getJobsTest(){
		List<Job> jobs=PersistentJob.getAllJobs(db);
                Assert.assertEquals("2 jobs",2,jobs.size());
                HashSet<JobId> ids=new HashSet<JobId>();
                ids.add(id);
                ids.add(idHigh);
		Assert.assertTrue("Contains job: "+jobs.get(0).getId(),ids.contains(jobs.get(0).getContext().getId()));
		Assert.assertTrue("Contains job: "+jobs.get(1).getId(),ids.contains(jobs.get(1).getContext().getId()));

	}

}
