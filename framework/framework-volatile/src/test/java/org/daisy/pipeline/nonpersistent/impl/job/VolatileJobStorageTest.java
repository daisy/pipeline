package org.daisy.pipeline.nonpersistent.impl.job;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.util.HashSet;
import java.util.Set;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.RuntimeConfigurator;
import org.daisy.pipeline.nonpersistent.impl.webservice.VolatileClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class VolatileJobStorageTest {
        JobContext ctxt1;
        JobContext ctxt2;
        JobContext ctxt1OtherCli;
        JobContext ctxt2OtherCli;
        VolatileJobStorage storage;
        VolatileClient cl = new VolatileClient("paco","asdf",Role.CLIENTAPP,"afasd",Priority.LOW);
        VolatileClient clOther = new VolatileClient("pepe","asdf",Role.CLIENTAPP,"afasd",Priority.LOW);
        VolatileClient clAdmin = new VolatileClient("power_paco","asdf",Role.ADMIN,"afasd",Priority.LOW);
        String oldBase = "";
        JobBatchId batchId1=JobIdFactory.newBatchId();
        JobBatchId batchId2=JobIdFactory.newBatchId();

        @Before
        public void setUp() {
                oldBase = System.getProperty("org.daisy.pipeline.iobase", "");
                System.setProperty("org.daisy.pipeline.iobase",
                                System.getProperty("java.io.tmpdir"));
                storage = new VolatileJobStorage();
                storage.setConfigurator(new RuntimeConfigurator());
                ctxt1 = Mock.newJobContext(cl,batchId1);
                ctxt2 = Mock.newJobContext(cl,batchId2);
                ctxt1OtherCli = Mock.newJobContext(clOther,batchId1);
                ctxt2OtherCli = Mock.newJobContext(clOther,batchId2);
        }

        @After
        public void tearDown() {
                System.setProperty("org.daisy.pipeline.iobase", oldBase);
        }

        @Test
        public void add() {
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxt1);
                Assert.assertTrue("The job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id",ctxt1.getId(),job.get().getId());

        }


        @Test
        public void get() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                Optional<Job> job = this.storage.get(ctxt1.getId());
                Assert.assertTrue("The job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id",ctxt1.getId(),job.get().getId());
        }

        @Test
        public void getNotPresent() {
                Optional<Job> job = this.storage.get(ctxt1.getId());
                Assert.assertFalse("The job is not present",job.isPresent());
        }

        @Test
        public void iterator() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                this.storage.add(Priority.MEDIUM,ctxt2);
                Set<JobId> jobs=new HashSet<JobId>();
                for(Job j: this.storage){
                        jobs.add(j.getId());
                }
                Assert.assertEquals(2,jobs.size());
                Assert.assertTrue(jobs.contains(ctxt1.getId()));
                Assert.assertTrue(jobs.contains(ctxt2.getId()));
        }

        @Test
        public void remove() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                this.storage.remove(ctxt1.getId());
                Optional<Job> job = this.storage.get(ctxt1.getId());
                Assert.assertFalse("The job has been removed",job.isPresent());
        }

        @Test
        public void removeNotPresent() {
                Optional<Job> job=this.storage.remove(ctxt1.getId());
                Assert.assertFalse("The job can't be removed as it's not in storage",job.isPresent());
        }

        @Test
        public void adminVsClientApp() throws Exception{

                Assert.assertEquals("Admin has all the rights",this.storage,this.storage.filterBy(this.clAdmin));
                Assert.assertThat("Filter by client app gives another storage",this.storage,is(not(this.storage.filterBy(this.cl))));

        }

        @Test
        public void getByClient() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                this.storage.add(Priority.MEDIUM,ctxt1OtherCli);
                Optional<Job> job = this.storage.filterBy(this.cl).get(ctxt1.getId());
                Assert.assertTrue("My job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id",ctxt1.getId(),job.get().getId());

                job = this.storage.filterBy(this.cl).get(ctxt1OtherCli.getId());
                Assert.assertFalse("But I can't see other client's jobs",job.isPresent());
        }

        @Test
        public void removeByClient() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                this.storage.add(Priority.MEDIUM,ctxt1OtherCli);
                Optional<Job> job = this.storage.filterBy(this.cl).remove(ctxt1OtherCli.getId());
                Assert.assertFalse("Can't delete a job that is not mine",job.isPresent());
                job = this.storage.filterBy(this.clAdmin).remove(ctxt1OtherCli.getId());
                Assert.assertTrue("But I can access delete it for an admin",job.isPresent());

                job = this.storage.get(ctxt1OtherCli.getId());
                Assert.assertFalse("And it is gone!",job.isPresent());
        }

        @Test
        public void iteratorByClient() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                this.storage.add(Priority.MEDIUM,ctxt2);
                this.storage.add(Priority.MEDIUM,ctxt1OtherCli);
                this.storage.add(Priority.MEDIUM,ctxt2OtherCli);
                Assert.assertEquals("From a cli perspective there are 2 jobs",2,Iterables.size(this.storage.filterBy(this.cl)));
                Assert.assertEquals("From an admin perspective there are 4 jobs",4,Iterables.size(this.storage.filterBy(this.clAdmin)));
        }
        @Test
        public void getByBatchId() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                this.storage.add(Priority.MEDIUM,ctxt2);
                this.storage.add(Priority.MEDIUM,ctxt1OtherCli);
                Optional<Job> job = this.storage.filterBy(this.batchId1).get(ctxt1.getId());
                Assert.assertTrue("My job has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected id",ctxt1.getId(),job.get().getId());

                job = this.storage.filterBy(this.batchId2).get(ctxt2.getId());
                Assert.assertTrue("My job 2 has been inserted",job.isPresent());
                Assert.assertEquals("And has the expected batch id (2)",ctxt2.getId(),job.get().getId());

                job = this.storage.filterBy(this.batchId2).get(ctxt1.getId());
                Assert.assertFalse("But I can't see other batch jobs",job.isPresent());
        }

        @Test
        public void iteratorByBatchId() {
                this.storage.add(Priority.MEDIUM,ctxt1);
                this.storage.add(Priority.MEDIUM,ctxt2);
                this.storage.add(Priority.MEDIUM,ctxt1OtherCli);
                this.storage.add(Priority.MEDIUM,ctxt2OtherCli);
                Assert.assertEquals("From a batch perspective there are 2 jobs",2,Iterables.size(this.storage.filterBy(this.batchId1)));
        }

}
