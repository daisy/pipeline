

package org.daisy.pipeline.persistence.impl.job;

import java.io.File;
import java.io.IOException;

import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.io.Files;

@RunWith(MockitoJUnitRunner.class)
public class ClientFilterTest{
        AbstractJob job;
        AbstractJob job2;
        Database db;
        PersistentClient client;
        File tempDir;
        File tempDir2;

        @Before
        public void setUp(){
		db=DatabaseProvider.getDatabase();
		tempDir = Files.createTempDir();
		tempDir2 = Files.createTempDir();
		job = new PersistentJob(db, Mocks.buildJob(tempDir), null);
		job2 = new PersistentJob(db, Mocks.buildJob(tempDir2), null);
                client=new PersistentClient("cli","sadfsa",Role.ADMIN,"asdf",Priority.LOW);
        }
	@After
	public void tearDown(){
		try {
			db.deleteObject(job);
			db.deleteObject(job.getContext().getClient());
			db.deleteObject(job2);
			db.deleteObject(job2.getContext().getClient());
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
                Assert.assertEquals("And it should be this one", job.getId().toString(), q.getSingleResult().getId().toString());
        }
}
