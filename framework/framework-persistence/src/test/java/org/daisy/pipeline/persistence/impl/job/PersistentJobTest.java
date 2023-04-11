package org.daisy.pipeline.persistence.impl.job;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.persistence.impl.Database;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class PersistentJobTest   {

	Database db;
	JobId id;
	JobId idHigh;
	PersistentJob job;
	PersistentJob jobHigh;
	File tempDir;
	File tempDir2;
	@Before	
	public void setUp(){
		db=DatabaseProvider.getDatabase();
		tempDir = Files.createTempDir();
		tempDir2 = Files.createTempDir();
		job = new PersistentJob(db, Mocks.buildJob(Priority.MEDIUM, tempDir), null);
		id=job.getContext().getId();
		// high priority
		jobHigh = new PersistentJob(db, Mocks.buildJob(Priority.HIGH, tempDir2), null);
		idHigh=jobHigh.getContext().getId();
	}
	@After
	public void tearDown(){
		try {
			db.deleteObject(job);
			db.deleteObject(job.getContext().getClient());
			db.deleteObject(jobHigh);
			db.deleteObject(jobHigh.getContext().getClient());
		} finally {
			if (tempDir != null)
				try {
					FileUtils.deleteDirectory(tempDir);
				} catch (IOException e) {
				}
			if (tempDir2 != null)
				try {
					FileUtils.deleteDirectory(tempDir2);
				} catch (IOException e) {
				}
		}
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
