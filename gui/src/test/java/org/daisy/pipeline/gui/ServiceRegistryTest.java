package org.daisy.pipeline.gui;

import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.script.ScriptRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRegistryTest {
        @Mock ScriptRegistry scriptRegistry;
        @Mock JobManagerFactory jobManagerFactory;
        @Mock WebserviceStorage webserviceStorage;
        @Mock DatatypeRegistry datatypeRegistry;
        @Mock GUIService guiService;
        @Mock PipelineApplication app;
        @Before
        public void setUp() {
        }
        @Test
        public void testNotify() {
                ServiceRegistry reg = ServiceRegistry.getInstance();
                Thread t = new Thread() {
                        public void run() {
                                try {
                                        reg.waitUntilReady();
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        Assert.fail("Unexpected exception");
                                }
                        }
                };
                t.start();
                reg.setScriptRegistry(scriptRegistry);
                Assert.assertTrue(t.isAlive());
                reg.setJobManagerFactory(jobManagerFactory);
                Assert.assertTrue(t.isAlive());
                reg.setDatatypeRegistry(datatypeRegistry);
                Assert.assertTrue(t.isAlive());
                reg.setWebserviceStorage(webserviceStorage);
                try {
                        t.join(500L);
                } catch (InterruptedException e) {
                }
                Assert.assertTrue("reg should not be ready yet", t.isAlive());
                reg.setGUIService(guiService);
                try {
                        t.join(500L);
                } catch (InterruptedException e) {
                }
                Assert.assertTrue("reg should be ready", !t.isAlive());
        }

}
