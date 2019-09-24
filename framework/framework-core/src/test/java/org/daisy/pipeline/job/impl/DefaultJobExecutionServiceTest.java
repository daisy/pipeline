package org.daisy.pipeline.job.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.daisy.common.fuzzy.InferenceEngine;
import org.daisy.common.priority.Priority;
import org.daisy.common.priority.PriorityThreadPoolExecutor;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Monitor;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJobExecutionServiceTest {

        @Rule
        public TestRule benchmarkRun = new BenchmarkRule();
        static int execCount=0;
        static char[] animation=new char[]{'|','/','-','\\'};
        Job[] jobs = new Job[100];
        DefaultJobExecutionService service;
        RunnableTracker tracker;
        Monitor mon;
        Monitor.Guard guard;
        @Mock Runnable runnable;
        @Mock InferenceEngine engine;
        
        private static char getChar(){
                execCount++;
                return animation[execCount%4];

        }

        @BeforeClass
        static public void warning(){
                System.out.println("Checking thread safety, this may take a while...");
        }
        //@Before
        public void setUp() {
                mon = new Monitor();
                tracker = new RunnableTracker();
                service = Mockito.spy(new DefaultJobExecutionService());
                jobs = new Job[100];
                for (int i=0;i<100;i++){
                        JobContext ctxt = Mockito.mock(JobContext.class);
                        Client client = Mockito.mock(Client.class);
                        Job job = Mockito.mock(Job.class);
                        JobId id = Mockito.mock(JobId.class);

                        Mockito.when(client.getPriority()).thenReturn(Priority.LOW);
                        Mockito.when(ctxt.getClient()).thenReturn(client);
                        Mockito.when(job.getContext()).thenReturn(ctxt);
                        Mockito.when(job.getPriority()).thenReturn(Priority.HIGH);
                        Mockito.when(job.getId()).thenReturn(id);
                        Mockito.when(id.toString()).thenReturn(
                                        String.format("%d",i));
                        //Mockito.when(id.equals(Mockito.any(JobId.class))).thenAnswer(new Answer<Boolean>() {
                                //public Boolean answer(InvocationOnMock invocation) {
                                        //Object[] args = invocation.getArguments();
                                        //return args[0].toString().equals(invocation.getMock().toString());
                                //}});
                        jobs[i] = job;
                        Mockito.doReturn(tracker.getRunnable(jobs[i])).when(service)
                                .getRunnable(jobs[i]);
                }

        }

        //@Test
        //public void simpleJobSubmission() {
                //service.submit(jobs[0]);
                //int executed = waitForSize(1, 100);
                //Assert.assertEquals("One task wasn't executed", 1, executed);

        //}
        
        //@Test
        //@BenchmarkOptions(benchmarkRounds = 50, warmupRounds = 0)
        //public void submitALot() {
                //System.out.print(getChar());
                //for (int i = 0; i < 100; i++) {
                        //service.submit(jobs[i]);
                //}
                //int executed = waitForSize(100, 2000);
                //System.out.print("\b");
                //Assert.assertEquals("One hundred tasks weren't executed", 100, executed);

        //}

        //@Test
        //@BenchmarkOptions(benchmarkRounds = 50, warmupRounds = 0)
        //public void submitALotAsynch() {
                //System.out.print(getChar());
                //for (int i = 0; i < 100; i++) {
                        //final int j = i;
                        //new Thread() {
                                //@Override
                                //public void run() {
                                        //service.submit(jobs[j]);
                                //}
                        //}.start();
                //}
                //int executed = waitForSize(100, 2000);
                //System.out.print("\b");
                //Assert.assertEquals("One hundred async tasks weren't executed", 100, executed);

        //}


        //public int waitForSize(final int size, long micro) {
                 //guard = new Monitor.Guard(mon) {

                        //@Override
                        //public boolean isSatisfied() {
                                //return tracker.visited().size() == size;

                        //}

                //};
                //boolean done=false;
                //try {
                        //done=mon.enterWhen(guard,2,TimeUnit.SECONDS);
                //} catch (InterruptedException e) {

                        //throw new RuntimeException(e);
                //}finally{
                        //mon.leave();
                        //if(!done){
                                //throw new RuntimeException("Waited for too long");
                        //}
                //}

                //return tracker.visited().size();
        //}

        class RunnableTracker {

                List<Job> visited = Lists.newLinkedList();
                List<Integer> ids= Lists.newLinkedList();

                public Runnable getRunnable(final Job job) {
                        return new Runnable(){
                                @Override
                                public void run(){

                                        mon.enter();
                                        synchronized(RunnableTracker.this.visited){
                                                visited.add(job);
                                        }
                                        ids.add(Integer.valueOf(job.getId().toString()));
                                        mon.leave();
                                }
                        };
                }

                public List<Job> visited(){
                        synchronized(this.visited){
                                return ImmutableList.copyOf(this.visited);
                        }
                }
        }
        @Test
        public void buildExecutor() {
                String old=System.getProperty(DefaultJobExecutionService.NUM_PROCS);
                //No config 
                System.setProperty(DefaultJobExecutionService.NUM_PROCS,"");
                PriorityThreadPoolExecutor<Job> res=DefaultJobExecutionService.configureExecutor();
                Assert.assertEquals("2 threads by default",res.getMaximumPoolSize(),2);
                //configured to other value
                System.setProperty(DefaultJobExecutionService.NUM_PROCS,"5");
                res=DefaultJobExecutionService.configureExecutor();
                Assert.assertEquals("configured for 5 threads",res.getMaximumPoolSize(),5);
                //nonsense 
                System.setProperty(DefaultJobExecutionService.NUM_PROCS,"gimme chocolate!!");
                res=DefaultJobExecutionService.configureExecutor();
                Assert.assertEquals("non int is trated as 2",res.getMaximumPoolSize(),2);





        }


}
