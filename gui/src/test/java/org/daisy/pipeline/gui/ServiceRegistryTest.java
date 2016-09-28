package org.daisy.pipeline.gui;

import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRegistryTest {
        @Mock  ScriptRegistry scriptRegistry;
        @Mock  JobManagerFactory jobManagerFactory;
        @Mock  EventBusProvider eventBusProvider;
        @Mock WebserviceStorage webserviceStorage;
        @Mock PipelineApplication app;
        @Before
        public void setUp(){
        }
        @Test
        public void testNotify(){
                ServiceRegistry reg=ServiceRegistry.getInstance();
                Thread t=new Thread(){
                        public void run(){
                                try {
                                        reg.notifyReady(app);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        Assert.fail("Unexpected exception");
                                }
                        }
                };
                t.start();
                reg.setEventBusProvider(eventBusProvider);
                Mockito.verify(app, Mockito.times(0)).setServiceRegistry(reg);
                reg.setScriptRegistry(scriptRegistry);
                Mockito.verify(app, Mockito.times(0)).setServiceRegistry(reg);
                reg.setJobManagerFactory(jobManagerFactory);
                Mockito.verify(app, Mockito.times(0)).setServiceRegistry(reg);
                reg.setWebserviceStorage(webserviceStorage);
                try {
                        t.join(100L, 0);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                        Assert.fail("Waiting for the thread to join");
                }
                Mockito.verify(app, Mockito.times(1)).setServiceRegistry(reg);
        }

}
