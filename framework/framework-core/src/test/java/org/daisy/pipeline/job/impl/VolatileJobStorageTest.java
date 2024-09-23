package org.daisy.pipeline.job.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.util.HashSet;
import java.util.Set;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.impl.VolatileJobStorage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class VolatileJobStorageTest {

        AbstractJob job1;
        AbstractJob job2;
        AbstractJob job1OtherCli;
        AbstractJob job2OtherCli;
        VolatileJobStorage storage;
        VolatileClient cl = new VolatileClient("paco", Role.CLIENTAPP, Priority.LOW);
        VolatileClient clOther = new VolatileClient("pepe", Role.CLIENTAPP, Priority.LOW);
        VolatileClient clAdmin = new VolatileClient("power_paco", Role.ADMIN, Priority.LOW);
        JobBatchId batchId1=JobIdFactory.newBatchId();
        JobBatchId batchId2=JobIdFactory.newBatchId();

        @Before
        public void setUp() {
                storage = new VolatileJobStorage();
                job1 = new VolatileJob(new AbstractJob(new Mock.MockedJobContext(cl, batchId1), Priority.MEDIUM, null, null, true) {});
                job2 = new VolatileJob(new AbstractJob(new Mock.MockedJobContext(cl, batchId2), Priority.MEDIUM, null, null, true) {});
                job1OtherCli = new VolatileJob(new AbstractJob(new Mock.MockedJobContext(clOther, batchId1), Priority.MEDIUM, null, null, true) {});
                job2OtherCli = new VolatileJob(new AbstractJob(new Mock.MockedJobContext(clOther, batchId2), Priority.MEDIUM, null, null, true) {});
        }

        @Test
        public void add() {
                Optional<AbstractJob> job = this.storage.add(job1);
                Assert.assertTrue("The job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id", job1.getId(), job.get().getId());
        }

        @Test
        public void get() {
                this.storage.add(job1);
                Optional<AbstractJob> job = this.storage.get(job1.getId());
                Assert.assertTrue("The job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id",job1.getId(),job.get().getId());
        }

        @Test
        public void getNotPresent() {
                Optional<AbstractJob> job = this.storage.get(job1.getId());
                Assert.assertFalse("The job is not present",job.isPresent());
        }

        @Test
        public void iterator() {
                this.storage.add(job1);
                this.storage.add(job2);
                Set<JobId> jobs=new HashSet<JobId>();
                for(Job j: this.storage){
                        jobs.add(j.getId());
                }
                Assert.assertEquals(2,jobs.size());
                Assert.assertTrue(jobs.contains(job1.getId()));
                Assert.assertTrue(jobs.contains(job2.getId()));
        }

        @Test
        public void remove() {
                this.storage.add(job1);
                this.storage.remove(job1.getId());
                Optional<AbstractJob> job = this.storage.get(job1.getId());
                Assert.assertFalse("The job has been removed",job.isPresent());
        }

        @Test
        public void removeNotPresent() {
                Optional<AbstractJob> job = this.storage.remove(job1.getId());
                Assert.assertFalse("The job can't be removed as it's not in storage",job.isPresent());
        }

        @Test
        public void adminVsClientApp() throws Exception{

                Assert.assertEquals("Admin has all the rights",this.storage,this.storage.filterBy(this.clAdmin));
                Assert.assertThat("Filter by client app gives another storage",this.storage,is(not(this.storage.filterBy(this.cl))));

        }

        @Test
        public void getByClient() {
                this.storage.add(job1);
                this.storage.add(job1OtherCli);
                Optional<AbstractJob> job = this.storage.filterBy(this.cl).get(job1.getId());
                Assert.assertTrue("My job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id", job1.getId(), job.get().getId());
                job = this.storage.filterBy(this.cl).get(job1OtherCli.getId());
                Assert.assertFalse("But I can't see other client's jobs",job.isPresent());
        }

        @Test
        public void removeByClient() {
                this.storage.add(job1);
                this.storage.add(job1OtherCli);
                Optional<AbstractJob> job = this.storage.filterBy(this.cl).remove(job1OtherCli.getId());
                Assert.assertFalse("Can't delete a job that is not mine",job.isPresent());
                job = this.storage.filterBy(this.clAdmin).remove(job1OtherCli.getId());
                Assert.assertTrue("But I can access delete it for an admin",job.isPresent());
                job = this.storage.get(job1OtherCli.getId());
                Assert.assertFalse("And it is gone!",job.isPresent());
        }

        @Test
        public void iteratorByClient() {
                this.storage.add(job1);
                this.storage.add(job2);
                this.storage.add(job1OtherCli);
                this.storage.add(job2OtherCli);
                Assert.assertEquals("From a cli perspective there are 2 jobs",2,Iterables.size(this.storage.filterBy(this.cl)));
                Assert.assertEquals("From an admin perspective there are 4 jobs",4,Iterables.size(this.storage.filterBy(this.clAdmin)));
        }
        @Test
        public void getByBatchId() {
                this.storage.add(job1);
                this.storage.add(job2);
                this.storage.add(job1OtherCli);
                Optional<AbstractJob> job = this.storage.filterBy(this.batchId1).get(job1.getId());
                Assert.assertTrue("My job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id", job1.getId(), job.get().getId());
                job = this.storage.filterBy(this.batchId2).get(job2.getId());
                Assert.assertTrue("My job 2 has been inserted", job.isPresent());
                Assert.assertEquals("And has the expected batch id (2)", job2.getId(), job.get().getId());
                job = this.storage.filterBy(this.batchId2).get(job1.getId());
                Assert.assertFalse("But I can't see other batch jobs", job.isPresent());
        }

        @Test
        public void iteratorByBatchId() {
                this.storage.add(job1);
                this.storage.add(job2);
                this.storage.add(job1OtherCli);
                this.storage.add(job2OtherCli);
                Assert.assertEquals("From a batch perspective there are 2 jobs",2,Iterables.size(this.storage.filterBy(this.batchId1)));
        }

}
