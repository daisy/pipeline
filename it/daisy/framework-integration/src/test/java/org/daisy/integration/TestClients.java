package org.daisy.integration;

import java.io.IOException;
import java.util.List;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.clients.Client;
import org.daisy.pipeline.webservice.jaxb.clients.Clients;
import org.daisy.pipeline.webservice.jaxb.clients.Priority;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class TestClients {
        private static final Logger logger = LoggerFactory.getLogger(TestClients.class);
        private static PipelineClient CLIENT=Utils.getClient();
        private static PipelineLauncher LAUNCHER;
        private static Client TEST_CLIENT_ADMIN;
        private static Client TEST_CLIENT_APP;
        static{
                TEST_CLIENT_ADMIN=new Client();
                TEST_CLIENT_ADMIN.setId("myadmin");
                TEST_CLIENT_ADMIN.setRole("ADMIN");
                TEST_CLIENT_ADMIN.setSecret("mysecret");
                TEST_CLIENT_ADMIN.setContact("admin@daisy.org");
                TEST_CLIENT_ADMIN.setPriority(Priority.HIGH);

                TEST_CLIENT_APP=new Client();
                TEST_CLIENT_APP.setId("myclient");
                TEST_CLIENT_APP.setRole("CLIENTAPP");
                TEST_CLIENT_APP.setSecret("shh");
                TEST_CLIENT_APP.setContact("client@daisy.org");
                TEST_CLIENT_APP.setPriority(Priority.LOW);
        }

        List<Client> toDelete;
        @BeforeClass 
        public static void bringUp() throws IOException {
                System.setProperty("enableLogging", "true");
                LAUNCHER=Utils.startPipeline(CLIENT);
                boolean up=LAUNCHER.launch();
                Assert.assertTrue("The pipeline is up",up);
        }

        @Before
        public void setUp() {
                this.toDelete=Lists.newLinkedList();
        }

        @After
        public void tearDown() throws Exception{
                for (Client c:this.toDelete){
                        CLIENT.deleteClient(c.getId());
                }
        }

        @AfterClass
        public static void bringDown() throws IOException {
                LAUNCHER.halt();
        }

        @Test
        public void testDefaultClientNotShown() throws Exception {
                logger.info("testDefaultClientNotShown IN");
                Clients clients=CLIENT.clients();
                Assert.assertEquals("Default client is not listed (but is there I swear!)",clients.getClient().size(),0);
                logger.info("testDefaultClientNotShown OUT");
        }
        @Test
        public void testAddClient() throws Exception {
                logger.info("addClient IN");
                Client client=CLIENT.addClient(TEST_CLIENT_ADMIN);
                toDelete.add(client);
                //the value returned by the server
                compareClients(TEST_CLIENT_ADMIN,client);
                //and the we list it
                compareClients(CLIENT.clients().getClient().get(0),TEST_CLIENT_ADMIN);
                logger.info("addClient OUT");
        }

        @Test
        public void testGetClient() throws Exception {
                logger.info("GetClient IN");
                CLIENT.addClient(TEST_CLIENT_ADMIN);
                toDelete.add(TEST_CLIENT_ADMIN);

                //client client, client client client!! https://www.youtube.com/watch?v=yL_-1d9OSdk
                Client client=CLIENT.client(TEST_CLIENT_ADMIN.getId());
                //the value returned by the server
                compareClients(TEST_CLIENT_ADMIN,client);
                //and the we list it
                compareClients(CLIENT.clients().getClient().get(0),TEST_CLIENT_ADMIN);
                logger.info("GetClient OUT");
        }

        @Test
        public void testDeleteClient() throws Exception {
                logger.info("DeleteClient IN");
                Client client=CLIENT.addClient(TEST_CLIENT_ADMIN);
                CLIENT.deleteClient(client.getId());

                try{
                        CLIENT.client(TEST_CLIENT_ADMIN.getId());
                        Assert.fail("Get client should throw an exception in here");
                }catch (Exception e){
                }
                logger.info("DeleteClient  OUT");
        }
        
        private void compareClients(Client exp,Client ret){
                Assert.assertEquals("Client id",exp.getId(),ret.getId());
                Assert.assertEquals("Client secret",exp.getSecret(),ret.getSecret());
                Assert.assertEquals("Client role",exp.getRole(),ret.getRole());
                Assert.assertEquals("Client contact",exp.getContact(),ret.getContact());
                //Assert.assertEquals("Client priority",exp.getPriority(),ret.getPriority());
        }

        @Test
        public void testModifyClient() throws Exception {
                logger.info("testModifyClient IN");
                Client client=CLIENT.addClient(TEST_CLIENT_APP);
                toDelete.add(client);
                Client modified=new Client();
                modified.setId(TEST_CLIENT_APP.getId());
                modified.setSecret(TEST_CLIENT_APP.getSecret());
                modified.setRole("ADMIN");
                modified.setContact("other@daisy.org");
                modified.setPriority(TEST_CLIENT_APP.getPriority());
                Client fromServer=CLIENT.updateClient(modified);
                //the value returned by the server
                compareClients(fromServer,modified);
                logger.info("testModifyClient OUT");
        }
}
