package org.daisy.integration;

import java.io.IOException;
import java.util.List;

import org.daisy.pipeline.webservice.jabx.job.Job;
import org.daisy.pipeline.webservice.jabx.job.JobSizes;
import org.daisy.pipeline.webservice.jabx.properties.Properties;
import org.daisy.pipeline.webservice.jabx.request.JobRequest;
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

public class TestAdmin {
        private static final Logger logger = LoggerFactory.getLogger(TestAdmin.class);
        private static PipelineClient CLIENT=Utils.getClient();
        private static PipelineLauncher LAUNCHER;
        List<Job> toDelete;

        private static PipelineClient getClient(){
                return CLIENT;
        }
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
        public static void bringUp() throws Exception {
                LAUNCHER=Utils.startPipeline(getClient());
                boolean up=LAUNCHER.launch();
                Assert.assertTrue("The pipeline is up",up);
                for (Job j:Utils.getClient().jobs().getJob()){
                        getClient().delete(j.getId());
                }
        }

        @AfterClass
        public static void bringDown() throws Exception {
                for (Job j:getClient().jobs().getJob()){
                        getClient().delete(j.getId());
                }
                LAUNCHER.halt();
        }

        @Test
        public void testProperties() throws Exception {
                Properties props=CLIENT.properties();
                Assert.assertTrue("We have properties",0<props.getProperty().size());

        }

        @Test
        public void testSizes() throws Exception {
                JobSizes sizes=CLIENT.sizes();
                Assert.assertEquals("The current size is 0",sizes.getTotal(),0);
                Assert.assertEquals("there are no jobs",sizes.getJobSize().size(),0);

                Optional<JobRequest> req = Utils.getJobRequest(getClient());
                Assert.assertTrue("Couldn't build the request",req.isPresent());

                Job job=getClient().sendJob(req.get());
                this.toDelete.add(job);
                Utils.waitForStatusChange("DONE",job,100000,getClient());
                sizes=CLIENT.sizes();
                Assert.assertFalse("The current size is not 0",sizes.getTotal()==0);
                Assert.assertEquals("there is one job",sizes.getJobSize().size(),1);

        }
}
