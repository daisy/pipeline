package org.daisy.pipeline.persistence.impl.job;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.RuntimeConfigurator;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScriptService;
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
import com.google.common.eventbus.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class PersistentJobStorageTest {
        List<Job> jobsToDel= Lists.newLinkedList(); 
        List<Client> clientsToDel= Lists.newLinkedList(); 
        Database db;
        PersistentJobStorage storage;
        PersistentClient cl = new PersistentClient("paco","asdf",Role.CLIENTAPP,"afasd",Priority.LOW);
        PersistentClient clAdmin = new PersistentClient("power_paco","asdf",Role.ADMIN,"afasd",Priority.LOW);
        @Mock XProcScriptService script;
        @Mock ScriptRegistry registry;
        @Mock RuntimeConfigurator configurator;
        @Mock EventBus bus;

        @Before
        public void setUp() {
		db=DatabaseProvider.getDatabase();
		System.setProperty("org.daisy.pipeline.iobase",System.getProperty("java.io.tmpdir"));
                storage=new PersistentJobStorage();
                storage.setEntityManagerFactory(DatabaseProvider.getEMF());
                storage.setRegistry(new Mocks.DummyScriptService(Mocks.buildScript()));
                storage.setConfigurator(configurator);
                Mockito.when(registry.getScript(Mockito.any(String.class))).thenReturn(script);
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
                for (Job j: jobsToDel){
                        this.db.deleteObject(j);
                }
                for (Client c : clientsToDel) {
                        this. db.deleteObject(c);
                }
                
        }
        @Test
        public void addJob() throws Exception{
                JobContext ctxt=Mocks.buildContext((Client)cl);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxt);
                this.jobsToDel.add(job.get());
                Assert.assertTrue("Job was created",job.isPresent());
        }

        @Test
        public void getJob() throws Exception{
                JobContext ctxt=Mocks.buildContext((Client)cl);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxt);
                this.jobsToDel.add(job.get());

                Optional<Job> fromDatabase=this.storage.get(ctxt.getId());
                Assert.assertTrue("A job was found",fromDatabase.isPresent());
                Assert.assertEquals("And happens to be the one we're looking for",ctxt.getId(),fromDatabase.get().getId());
        }

        @Test
        public void getEmptyJob() throws Exception{
                JobContext ctxt=Mocks.buildContext((Client)cl);

                Optional<Job> fromDatabase=this.storage.get(ctxt.getId());
                Assert.assertFalse("The job shouldn't exist",fromDatabase.isPresent());
        }

        @Test
        public void remove() throws Exception{


                JobContext ctxt=Mocks.buildContext((Client)cl);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxt);
                this.jobsToDel.add(job.get());

                Optional<Job> deleted=this.storage.remove(ctxt.getId());
                Assert.assertTrue("The job's been removed",deleted.isPresent());

                Optional<Job> fromDatabase=this.storage.get(ctxt.getId());
                Assert.assertFalse("The job shouldn't exist no more",fromDatabase.isPresent());

        }

        @Test
        public void removeNonExisting() throws Exception{

                JobContext ctxt=Mocks.buildContext((Client)cl);

                Optional<Job> deleted=this.storage.remove(ctxt.getId());
                Assert.assertFalse("The job shouldn't be removed",deleted.isPresent());


        }

        @Test
        public void iterate() throws Exception{

                JobContext ctxt=Mocks.buildContext((Client)cl);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxt);
                this.jobsToDel.add(job.get());

                ctxt=Mocks.buildContext((Client)cl);
                job=this.storage.add(Priority.MEDIUM,ctxt);
                this.jobsToDel.add(job.get());

                Assert.assertEquals("Two jobs is what we have in store",2,Iterables.size(this.storage));
                
        }
        

        @Test
        public void byClientGetJob() throws Exception{
                //my client

                JobContext ctxtMine=Mocks.buildContext((Client)cl);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxtMine);
                this.jobsToDel.add(job.get());

                JobContext ctxtOther=Mocks.buildContext();
                job=this.storage.add(Priority.MEDIUM,ctxtOther);
                this.jobsToDel.add(job.get());
                this.clientsToDel.add(ctxtOther.getClient());

                Optional<Job> fromDatabase=this.storage.filterBy(this.cl).get(ctxtMine.getId());
                Assert.assertTrue("A job was found",fromDatabase.isPresent());
                Assert.assertEquals("And it's mine",ctxtMine.getId(),fromDatabase.get().getId());

                fromDatabase=this.storage.filterBy(this.cl).get(ctxtOther.getId());
                Assert.assertFalse("Someone else's job is not found",fromDatabase.isPresent());
        }

        @Test
        public void byClientRemoveJob() throws Exception{

                JobContext ctxtMine=Mocks.buildContext((Client)cl);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxtMine);
                this.jobsToDel.add(job.get());

                JobContext ctxtOther=Mocks.buildContext();
                job=this.storage.add(Priority.MEDIUM,ctxtOther);
                this.jobsToDel.add(job.get());
                this.clientsToDel.add(ctxtOther.getClient());

                Optional<Job> fromDatabase=this.storage.filterBy(this.cl).remove(ctxtMine.getId());
                Assert.assertTrue("A job was removed",fromDatabase.isPresent());

                fromDatabase=this.storage.filterBy(this.cl).remove(ctxtOther.getId());
                Assert.assertFalse("Someone else's job is not removed",fromDatabase.isPresent());

                fromDatabase=this.storage.get(ctxtMine.getId());
                Assert.assertFalse("My job can't be found",fromDatabase.isPresent());

                fromDatabase=this.storage.get(ctxtOther.getId());
                Assert.assertTrue("But the other job is still there",fromDatabase.isPresent());

        }

        @Test
        public void byClientIterate() throws Exception{

                JobContext ctxtMine=Mocks.buildContext((Client)cl);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxtMine);
                this.jobsToDel.add(job.get());

                ctxtMine=Mocks.buildContext((Client)cl);
                job=this.storage.add(Priority.MEDIUM,ctxtMine);
                this.jobsToDel.add(job.get());

                JobContext ctxtOther=Mocks.buildContext();
                job=this.storage.add(Priority.MEDIUM,ctxtOther);
                this.jobsToDel.add(job.get());
                this.clientsToDel.add(ctxtOther.getClient());

                ctxtOther=Mocks.buildContext();
                job=this.storage.add(Priority.MEDIUM,ctxtOther);
                this.jobsToDel.add(job.get());
                this.clientsToDel.add(ctxtOther.getClient());

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
                //my client

                JobBatchId id1=JobIdFactory.newBatchId();
                JobBatchId id2=JobIdFactory.newBatchId();
                JobContext ctxtMineId1=Mocks.buildContext((Client)cl,id1);
                Optional<Job> job=this.storage.add(Priority.MEDIUM,ctxtMineId1);
                this.jobsToDel.add(job.get());

                JobContext ctxtOtherId2=Mocks.buildContext((Client)cl,id2);
                job=this.storage.add(Priority.MEDIUM,ctxtOtherId2);
                this.jobsToDel.add(job.get());
                this.clientsToDel.add(ctxtOtherId2.getClient());

                Optional<Job> fromDatabase=this.storage.filterBy(this.cl).filterBy(id1).get(ctxtMineId1.getId());
                Assert.assertTrue("A job was found",fromDatabase.isPresent());
                Assert.assertEquals("And it's from my batch",ctxtMineId1.getId(),fromDatabase.get().getId());

                fromDatabase=this.storage.filterBy(this.cl).filterBy(id1).get(ctxtOtherId2.getId());
                Assert.assertFalse("Someone else's job is not found",fromDatabase.isPresent());
        }

}
