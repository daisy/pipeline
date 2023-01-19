package org.daisy.pipeline.persistence.impl.job;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;
import org.daisy.pipeline.script.ScriptRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PersistentJobStorageTest {
        List<AbstractJob> jobsToDel= Lists.newLinkedList();
        List<Client> clientsToDel= Lists.newLinkedList(); 
        Database db;
        PersistentJobStorage storage;
        PersistentClient cl = new PersistentClient("paco","asdf",Role.CLIENTAPP,"afasd",Priority.LOW);
        PersistentClient clAdmin = new PersistentClient("power_paco","asdf",Role.ADMIN,"afasd",Priority.LOW);
        @Mock ScriptRegistry registry;
        String oldBase;

        @Before
        public void setUp() {
		db=DatabaseProvider.getDatabase();
                oldBase = System.getProperty("org.daisy.pipeline.data", "");
                System.setProperty("org.daisy.pipeline.data", System.getProperty("java.io.tmpdir"));
                storage=new PersistentJobStorage();
                storage.setEntityManagerFactory(DatabaseProvider.getEMF());
                storage.setRegistry(new Mocks.DummyScriptService(Mocks.buildScript()));
                db.addObject(this.cl);
                db.addObject(this.clAdmin);
                clientsToDel.add(this.cl);
                clientsToDel.add(this.clAdmin);
        }

        @After
        public void tearDown() {
                //for (Job j: jobsToDel){
                        //this.db.deleteObject(j.getContext());
                //}
                for (AbstractJob j: jobsToDel){
                        this.db.deleteObject(j);
                }
                for (Client c : clientsToDel) {
                        this. db.deleteObject(c);
                }
                System.setProperty("org.daisy.pipeline.data", oldBase);
        }
        @Test
        public void addJob() throws Exception{
                Optional<AbstractJob> job = this.storage.add(Mocks.buildJob((Client)cl));
                this.jobsToDel.add(job.get());
                Assert.assertTrue("Job was created",job.isPresent());
        }

        @Test
        public void getJob() throws Exception{
                AbstractJob job = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(job);
                Optional<AbstractJob> fromDatabase = this.storage.get(job.getId());
                Assert.assertTrue("A job was found",fromDatabase.isPresent());
                Assert.assertEquals("And happens to be the one we're looking for", job.getId(), fromDatabase.get().getId());
        }

        @Test
        public void getEmptyJob() throws Exception{
                AbstractJobContext ctxt = Mocks.buildContext((Client)cl);
                Optional<AbstractJob> fromDatabase = this.storage.get(ctxt.getId());
                Assert.assertFalse("The job shouldn't exist",fromDatabase.isPresent());
        }

        @Test
        public void remove() throws Exception{
                AbstractJob job = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(job);
                Optional<AbstractJob> deleted = this.storage.remove(job.getId());
                Assert.assertTrue("The job's been removed",deleted.isPresent());
                Optional<AbstractJob> fromDatabase = this.storage.get(job.getId());
                Assert.assertFalse("The job shouldn't exist no more",fromDatabase.isPresent());
        }

        @Test
        public void removeNonExisting() throws Exception{
                AbstractJobContext ctxt = Mocks.buildContext((Client)cl);
                Optional<AbstractJob> deleted = this.storage.remove(ctxt.getId());
                Assert.assertFalse("The job shouldn't be removed",deleted.isPresent());
        }

        @Test
        public void iterate() throws Exception{
                AbstractJob job = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(job);
                job = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(job);
                Assert.assertEquals("Two jobs is what we have in store",2,Iterables.size(this.storage));
                
        }

        @Test
        public void byClientGetJob() throws Exception{
                AbstractJob jobMine = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(jobMine);
                AbstractJob jobOther = this.storage.add(Mocks.buildJob()).get();
                this.jobsToDel.add(jobOther);
                this.clientsToDel.add(jobOther.getClient());
                Optional<AbstractJob> fromDatabase = this.storage.filterBy(this.cl).get(jobMine.getId());
                Assert.assertTrue("A job was found",fromDatabase.isPresent());
                Assert.assertEquals("And it's mine", jobMine.getId(),fromDatabase.get().getId());
                fromDatabase=this.storage.filterBy(this.cl).get(jobOther.getId());
                Assert.assertFalse("Someone else's job is not found",fromDatabase.isPresent());
        }

        @Test
        public void byClientRemoveJob() throws Exception{
                AbstractJob jobMine = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(jobMine);
                AbstractJob jobOther = this.storage.add(Mocks.buildJob()).get();
                this.jobsToDel.add(jobOther);
                this.clientsToDel.add(jobOther.getClient());
                Optional<AbstractJob> fromDatabase = this.storage.filterBy(this.cl).remove(jobMine.getId());
                Assert.assertTrue("A job was removed",fromDatabase.isPresent());
                fromDatabase=this.storage.filterBy(this.cl).remove(jobOther.getId());
                Assert.assertFalse("Someone else's job is not removed",fromDatabase.isPresent());
                fromDatabase=this.storage.get(jobMine.getId());
                Assert.assertFalse("My job can't be found",fromDatabase.isPresent());
                fromDatabase=this.storage.get(jobOther.getId());
                Assert.assertTrue("But the other job is still there",fromDatabase.isPresent());
        }

        @Test
        public void byClientIterate() throws Exception{
                AbstractJob jobMine = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(jobMine);
                jobMine = this.storage.add(Mocks.buildJob((Client)cl)).get();
                this.jobsToDel.add(jobMine);
                AbstractJob jobOther = this.storage.add(Mocks.buildJob()).get();
                this.jobsToDel.add(jobOther);
                this.clientsToDel.add(jobOther.getClient());
                jobOther = this.storage.add(Mocks.buildJob()).get();
                this.jobsToDel.add(jobOther);
                this.clientsToDel.add(jobOther.getClient());
                Assert.assertEquals("I have two jobs",2,Iterables.size(this.storage.filterBy(cl)));
                Assert.assertEquals("But everyone have four",4,Iterables.size(this.storage));

        }

        @Test
        public void adminVsClientApp() throws Exception{

                Assert.assertEquals("Admin has all the rights",this.storage,this.storage.filterBy(this.clAdmin));
                Assert.assertThat("Filter by client app gives another storage",this.storage,is(not(this.storage.filterBy(this.cl))));

        }
        @Test
        public void byClientAndBatchIdGetJob() throws Exception{
                JobBatchId id1=JobIdFactory.newBatchId();
                JobBatchId id2=JobIdFactory.newBatchId();
                AbstractJob jobMineId1 = this.storage.add(Mocks.buildJob((Client)cl, id1)).get();
                this.jobsToDel.add(jobMineId1);
                AbstractJob jobOtherId2 = this.storage.add(Mocks.buildJob((Client)cl, id2)).get();
                this.jobsToDel.add(jobOtherId2);
                this.clientsToDel.add(jobOtherId2.getClient());
                Optional<AbstractJob> fromDatabase = this.storage.filterBy(this.cl).filterBy(id1).get(jobMineId1.getId());
                Assert.assertTrue("A job was found",fromDatabase.isPresent());
                Assert.assertEquals("And it's from my batch", jobMineId1.getId(), fromDatabase.get().getId());
                fromDatabase=this.storage.filterBy(this.cl).filterBy(id1).get(jobOtherId2.getId());
                Assert.assertFalse("Someone else's job is not found",fromDatabase.isPresent());
        }

}
