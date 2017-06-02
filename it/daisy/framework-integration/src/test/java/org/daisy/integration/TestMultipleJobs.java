package org.daisy.integration;

import java.io.IOException;
import java.util.List;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.request.Priority;
import org.daisy.pipeline.webservice.jaxb.queue.Queue;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.Jobs;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class TestMultipleJobs {


        private static final Logger logger = LoggerFactory.getLogger(TestMultipleJobs.class);
        private static PipelineClient CLIENT=Utils.getClient();
        private static PipelineLauncher LAUNCHER;

        @BeforeClass 
        public static void bringUp() throws IOException {
                System.setProperty("enableLogging", "true");
                LAUNCHER=Utils.startPipeline(CLIENT);
                boolean up=LAUNCHER.launch();
                Assert.assertTrue("The pipeline is up",up);
        }

        @AfterClass
        public static void bringDown() throws IOException {
                LAUNCHER.halt();
        }
        
        //@Test
        //public void testMultipleJobs() throws Exception {
                //logger.info(String.format("%s testMultipleJobs IN",TestLocalJobs.class));
                //Optional<JobRequest> req = Utils.getJobRequest(CLIENT);
                ////send two jobs
                ////TODO: Adjust the number of jobs via properties to be sure
                //Job job1=CLIENT.sendJob(req.get());
                //Job job2=CLIENT.sendJob(req.get());
                //Job job3=CLIENT.sendJob(req.get());
                //Jobs jobs=CLIENT.jobs();
                //Assert.assertEquals("we have 3 jobs",jobs.getJob().size(),3);
                //Utils.waitForStatusChange("DONE",job1,100000,CLIENT);
                //CLIENT.delete(job1.getId());
                //CLIENT.delete(job2.getId());
                //CLIENT.delete(job3.getId());
                //logger.info(String.format("%s testMultipleJobs OUT",TestLocalJobs.class));
        //}
        @Test
        public void testPriorites() throws Exception {
                logger.info(String.format("%s testQueue IN",TestLocalJobs.class));
                List<Job> jobs =Lists.newLinkedList();
                Priority[] prios = new Priority[]{Priority.HIGH,Priority.HIGH,Priority.HIGH,
                       Priority.LOW,Priority.MEDIUM,Priority.HIGH};

                for (int i= 0; i<6;i++){
                        //remove the priority
                        Optional<JobRequest> req = Utils.getJobRequest(CLIENT,prios[i]);
                        jobs.add(CLIENT.sendJob(req.get()));
                        if (i==2){//wait to have a more equal relative time for the next 3 jobs
                                try {
                                        Thread.sleep(1000);                 
                                } catch(InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                }
                        }
                }
                
                try{
                        List<org.daisy.pipeline.webservice.jaxb.queue.Job> queue =CLIENT.queue().getJob();
                        //printQueue(queue);
                        org.daisy.pipeline.webservice.jaxb.queue.Job last=queue.get(queue.size()-1);
                        //As the algorithm is time dependent it has different behaviours depending
                        //on the machine this test is exectuted 
                        //Assert.assertEquals("last job has priority low",last.getJobPriority().value(),"low");
                        //Assert.assertEquals("next to last job has priority medium",queue.get(queue.size()-2).getJobPriority().value(),"medium");
                        //Assert.assertEquals("first job has priority high",queue.get(queue.size()-3).getJobPriority().value(),"high");

                        queue=CLIENT.moveUp(last.getId()).getJob();
                        //printQueue(queue);
                        Assert.assertEquals("The last job has been moved up",last.getId(),queue.get(queue.size()-2).getId());
                        queue=CLIENT.moveDown(last.getId()).getJob();
                        //printQueue(queue);
                        Assert.assertEquals("The last job has been moved down",last.getId(),queue.get(queue.size()-1).getId());

                        Utils.waitForStatusChange("DONE",jobs.get(jobs.size()-1),100000,CLIENT);
                }finally{
                        for (Job j: jobs){
                                CLIENT.delete(j.getId());
                        }
                }
                logger.info(String.format("%s testQueue OUT",TestLocalJobs.class));
        }

        //private static void printQueue(List<org.daisy.pipeline.webservice.jaxb.queue.Job> queue){
                //for (org.daisy.pipeline.webservice.jaxb.queue.Job j:queue){
                        //System.out.println(String.format("Id: %s Job: %s Client:%s computed: %s time:%s ",j.getId(), j.getJobPriority().value(),j.getClientPriority().value(),j.getComputedPriority(),j.getRelativeTime()));
                //}
                //System.out.println("+++++++++++++++++++++++++++++++++");
        //}

}
