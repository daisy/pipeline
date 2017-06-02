package org.daisy.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.Response;

import org.daisy.pipeline.webservice.jabx.base.Alive;
import org.daisy.pipeline.webservice.jabx.job.Job;
import org.daisy.pipeline.webservice.jabx.job.Result;
import org.daisy.pipeline.webservice.jabx.request.JobRequest;
import org.daisy.pipeline.webservice.jabx.request.Priority;
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
import com.google.common.io.CharStreams;

public class TestNonLocalJobs {

        private static final Logger logger = LoggerFactory.getLogger(TestNonLocalJobs.class);
        private static PipelineClient CLIENT=Utils.getClient();
        private static PipelineLauncher LAUNCHER;

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
                                CLIENT.delete(j.getId());
                        }catch (Exception e){
                                logger.info(e.getMessage());

                        }
                }
                logger.info(String.format("There are %s jobs after the test", CLIENT.jobs().getJob().size()));

        }
        @BeforeClass
        public static void bringUp() throws IOException {
                LAUNCHER=Utils.startPipeline(CLIENT);
                LAUNCHER.setEnv("PIPELINE2_LOCAL","false");
                boolean up=LAUNCHER.launch();
                Assert.assertTrue("The pipeline is up",up);
        }

        @AfterClass
        public static void bringDown() throws Exception {
                System.setProperty("enableLogging", "true");
                for (Job j:CLIENT.jobs().getJob()){
                        CLIENT.delete(j.getId());
                }
                LAUNCHER.halt();
        }

        @Test
        public void testAlive() throws Exception {
                logger.info(String.format("%s testAlive IN",TestNonLocalJobs.class));

                Alive alive = CLIENT.alive(); 
                Assert.assertTrue("The version is empty",alive.getVersion().length()>0);
                Assert.assertTrue("The ws doesn't accept local jobs",alive.getLocalfs().equalsIgnoreCase("false"));
                logger.info(String.format("%s testAlive OUT",TestNonLocalJobs.class));
        }

        @Test
        public void testSendJob() throws Exception {
                Optional<JobRequest> req=Utils.getJobRequest(CLIENT,Priority.MEDIUM,"./hauy_valid.xml");
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("dtbook/dtbook.zip");
                Assert.assertTrue("The request is present",req.isPresent());
                Job job=CLIENT.sendJob(req.get(),is);
                this.toDelete.add(job);
                Assert.assertTrue("Job has been sent",job.getId()!=null &&job.getId().length()>0);


        }
        @Test
        public void testResults() throws Exception {
                Optional<JobRequest> req=Utils.getJobRequest(CLIENT,Priority.MEDIUM,"hauy_valid.xml");
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("dtbook/dtbook.zip");
                Assert.assertTrue("The request is present",req.isPresent());
                Job job=CLIENT.sendJob(req.get(),is);
                this.toDelete.add(job);
                job=Utils.waitForStatusChange("DONE",job,100000,CLIENT);

                List<Result> results=new JobWrapper(job).getResults().getResult();
                //first level
                for( Result firstLevelResult:results){
                        checkZippedResult(firstLevelResult);

                        for (Result result: firstLevelResult.getResult()){
                                checkLeafResult(result);

                        }
                }



        }

        private void checkZippedResult(Result result) throws IOException {
                logger.info(String.format("Getting result %s",result.getHref()));
                Response response=CLIENT.get(result.getHref().replace(CLIENT.getBaseUri(),""));
                InputStream ris=response.readEntity(InputStream.class);
                ZipInputStream zis = new ZipInputStream(ris);
                Assert.assertNotNull("The zip file has entries",zis.getNextEntry());
                ris.close();
        }

        private void checkLeafResult(Result result) throws IOException {
                logger.info(String.format("Getting result %s",result.getHref()));
                Response response=CLIENT.get(result.getHref().replace(CLIENT.getBaseUri(),""));
                InputStream ris=response.readEntity(InputStream.class);
                String strRes=CharStreams.toString(new InputStreamReader(ris));
                Assert.assertTrue(String.format("The result has stuff %s", result.getHref()),strRes.length()>0);
                ris.close();
        }


}
