package org.daisy.integration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.base.Alive;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.Result;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.script.Scripts;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class TestLocalJobs {
        private static final Logger logger = LoggerFactory.getLogger(TestLocalJobs.class);
        private static PipelineClient CLIENT=Utils.getClient();
        private static PipelineLauncher LAUNCHER;

        private static PipelineClient getClient(){
                return CLIENT;
        }

        List<Job> toDelete;
        @Before
        public void setUp(){
                toDelete=Lists.newLinkedList();

        }
        @After
        public void tearDown(){
               logger.info(String.format("There are %s jobs to delete", toDelete.size()));
                for ( Job j:this.toDelete){
                        try{
                                logger.info(String.format("Deleting job %s",j.getId()));
                                getClient().delete(j.getId());
                        }catch (Exception e){
                                logger.info(e.getMessage());
                        
                        }
                }
               logger.info(String.format("There are %s jobs after the test", getClient().jobs().getJob().size()));

        }
        @BeforeClass
        public static void bringUp() throws IOException {
                LAUNCHER=Utils.startPipeline(getClient());
                boolean up=LAUNCHER.launch();
                Assert.assertTrue("The pipeline is up",up);
        }

        @AfterClass
        public static void bringDown() throws Exception {
            System.setProperty("enableLogging", "true");
                for (Job j:getClient().jobs().getJob()){
                        getClient().delete(j.getId());
                }
                LAUNCHER.halt();
        }

        @Test
        public void testAlive() throws Exception {
                logger.info(String.format("%s testAlive IN",TestLocalJobs.class));
        
                Alive alive = getClient().alive(); 
                Assert.assertTrue("The version is empty",alive.getVersion().length()>0);
                Assert.assertTrue("The ws doesn't accept local jobs",alive.getLocalfs().equalsIgnoreCase("true"));
                Assert.assertTrue("The ws needs credentials",alive.getAuthentication().equalsIgnoreCase("false"));
                logger.info(String.format("%s testAlive OUT",TestLocalJobs.class));
        }

        @Test
        public void testScripts() throws Exception {
                logger.info(String.format("%s testScripts IN",TestLocalJobs.class));
                Scripts scripts = getClient().scripts();
                Assert.assertTrue("There are no scripts in pipeline",scripts.getScript().size()>0);
                logger.info(String.format("%s testScripts OUT",TestLocalJobs.class));
        }

        @Test
        public void testSendJob() throws Exception {
                logger.info(String.format("%s testSendJob IN",TestLocalJobs.class));
                Optional<JobRequest> req = Utils.getJobRequest(getClient());
                
                Assert.assertTrue("Couldn't build the request",req.isPresent());
                Job job=getClient().sendJob(req.get());
                this.toDelete.add(job);
                Assert.assertTrue("Job has been sent",job.getId()!=null &&job.getId().length()>0);
                //So we don't over load the pipeline with different jobs
                checkJobInfo(job);
                logger.info(String.format("%s testSendJob OUT",TestLocalJobs.class));

        }

        private void checkJobInfo(Job in) throws Exception {
                Job job = getClient().job(in.getId());
                ////Check the id
                Assert.assertEquals("Ids are not equal",in.getId(),job.getId());
                Assert.assertEquals("Nice name is set",Utils.NICE_NAME,job.getNicenameOrScriptOrMessages().get(0));
                Assert.assertTrue("Status is set",job.getStatus().value().length()>0);
                Assert.assertEquals("The priority is low","low",job.getPriority().toString().toLowerCase());
                

        }

        @Test
        public void testJobStatusCycle() throws Exception {
                logger.info(String.format("%s testJobStatusCycle IN",TestLocalJobs.class));
                Optional<JobRequest> req = Utils.getJobRequest(getClient());
                //send two jobs
                //TODO: Adjust the number of jobs via properties to be sure
                this.toDelete.add(getClient().sendJob(req.get()));
                this.toDelete.add(getClient().sendJob(req.get()));
                Job job=getClient().sendJob(req.get());
                this.toDelete.add(job);
                Assert.assertEquals("The job status is IDLE",job.getStatus().value(),"IDLE");
                job=Utils.waitForStatusChange("RUNNING",job,100000,getClient());
                Assert.assertEquals("The job status is RUNNING",job.getStatus().value(),"RUNNING");
                job=Utils.waitForStatusChange("DONE",job,100000,getClient());
                Assert.assertEquals("The job status is DONE",job.getStatus().value(),"DONE");
                logger.info(String.format("%s testJobStatusCycle OUT",TestLocalJobs.class));
        
        }

        @Test
        public void testAfterJob() throws Exception {
                logger.info(String.format("%s testAfterJob IN",TestLocalJobs.class));
                Optional<JobRequest> req = Utils.getJobRequest(getClient());
                Job job=getClient().sendJob(req.get());
                this.toDelete.add(job);
                job=Utils.waitForStatusChange("DONE",job,100000,getClient());
                //test results
                checkResults(job);
                //tet logs
                checkLog(job);
                //test delete
                checkDelete(job);
                logger.info(String.format("%s testAfterJob OUT",TestLocalJobs.class));
        }

        private void checkDelete(Job in) throws Exception {
                logger.info(String.format("%s checking deletion",TestLocalJobs.class));
                PipelineClient client=getClient();
                client.delete(in.getId());
                try{
                        client.job(in.getId());
                        Assert.fail("The job shouldn't be here");
                }catch(javax.ws.rs.NotFoundException nfe){

                }

                File jobData=new File(Utils.jobPath(in.getId()));
                Assert.assertFalse("Make sure the data folder doesn't exist anymore",Files.isDirectory().apply(jobData));


        }

        private void checkLog(Job in) throws IOException {
                logger.info(String.format("%s checking log",TestLocalJobs.class));
                String fromServer=getClient().log(in.getId());
                File logFile=new File(Utils.logPath(in.getId()));
                String fromFile=Files.toString(logFile,Charset.defaultCharset());
                Assert.assertEquals("The log from the server and the file are equal",fromServer,fromFile);
        }


        private void checkResults(Job in) {
                logger.info(String.format("%s checking results",TestLocalJobs.class));
                List<Result> results=new JobWrapper(in).getResults().getResult();
                //first level
                for( Result firstLevelResult:results){
                        for (Result result: firstLevelResult.getResult()){
                                Assert.assertTrue(String.format("The file  %s exists",result.getFile()),
                                Files.isFile().apply(new File(URI.create(result.getFile()))));

                        }
                }


        }

 
       
}
