package org.daisy.integration;

import java.io.IOException;

import org.daisy.pipeline.client.PipelineClient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestAuthenticationErrors {

        @After
        public void tearDown() throws IOException {
            Utils.cleanUpDb();
        }

        @Test
        public void testNoClient() throws Exception {
                PipelineClient client=Utils.getClient(TestClientJobs.CREDS_DEF.clientId,TestClientJobs.CREDS_DEF.secret);
                PipelineLauncher launcher;
                launcher=Utils.startPipeline(client);
                launcher.setEnv("PIPELINE2_AUTH","true");
                //launcher.setProperty("org.daisy.pipeline.ws.authentication.key",TestClientJobs.CREDS_DEF.clientId);
                //launcher.setProperty("org.daisy.pipeline.ws.authentication.secret",TestClientJobs.CREDS_DEF.secret);

                boolean up=launcher.launch();
                Assert.assertFalse("The pipeline is not up",up);

        }
        @Test
        public void testAccessWithoutPermissions() throws Exception {
                PipelineClient client=Utils.getClient(TestClientJobs.CREDS_DEF.clientId,TestClientJobs.CREDS_DEF.secret);
                PipelineLauncher launcher;
                launcher=Utils.startPipeline(client);
                launcher.setEnv("PIPELINE2_AUTH","true");
                launcher.setProperty("org.daisy.pipeline.ws.authentication.key",TestClientJobs.CREDS_DEF.clientId);
                launcher.setProperty("org.daisy.pipeline.ws.authentication.secret",TestClientJobs.CREDS_DEF.secret);

                boolean up=launcher.launch();
                Assert.assertTrue("The pipeline is up",up);
                PipelineClient bogus=Utils.getClient("notallowed","whatevs");
                try {
                        bogus.scripts();
                        Assert.fail();
                } catch(Exception e){
                }
                launcher.halt();

        }
        
}
