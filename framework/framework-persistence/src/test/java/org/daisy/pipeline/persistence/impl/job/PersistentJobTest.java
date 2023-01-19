package org.daisy.pipeline.persistence.impl.job;

import java.util.HashSet;
import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.persistence.impl.Database;
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
		db=DatabaseProvider.getDatabase();
		job = new PersistentJob(db, Mocks.buildJob(Priority.MEDIUM), null);
		id=job.getContext().getId();
		// high priority
		jobHigh = new PersistentJob(db, Mocks.buildJob(Priority.HIGH), null);
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
                pjob.changeStatus(Job.Status.SUCCESS);
                pjob= db.getEntityManager().find(PersistentJob.class,id.toString());
                Assert.assertEquals(Job.Status.SUCCESS,pjob.getStatus());

        }
	@Test 
	public void getJobsTest(){
		List<AbstractJob> jobs=PersistentJob.getAllJobs(db);
                Assert.assertEquals("2 jobs",2,jobs.size());
                HashSet<JobId> ids=new HashSet<JobId>();
                ids.add(id);
                ids.add(idHigh);
		Assert.assertTrue("Contains job: "+jobs.get(0).getId(),ids.contains(jobs.get(0).getContext().getId()));
		Assert.assertTrue("Contains job: "+jobs.get(1).getId(),ids.contains(jobs.get(1).getContext().getId()));

	}

}
