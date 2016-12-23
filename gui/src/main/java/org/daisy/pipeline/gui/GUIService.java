package org.daisy.pipeline.gui;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GUIService 
{
        private static final Logger logger = LoggerFactory.getLogger(GUIService.class);
        private BundleContext ctxt;

        public void init(BundleContext ctxt) {
                this.ctxt=ctxt;
                ServiceRegistry.getInstance().setGUIService(this);
                //Otherwise launch will block
                new Thread(){
                        public void run(){
                                javafx.application.Application.launch(PipelineApplication.class);
                                
                        }
                }.start();
                logger.debug("Main Module is loaded!");
        }

        public void stopGUI(){
                try {
                        ((Framework) this.ctxt.getBundle(0)).stop();
                } catch (BundleException e) {
                        logger.error("Error closing the framework ",e);
                        //exit the hard way
                        System.exit(-1);
                }
        }



        public void setScriptRegistry(ScriptRegistry scriptRegistry) {
                ServiceRegistry.getInstance().setScriptRegistry(scriptRegistry);
        }
        public void setJobManagerFactory(JobManagerFactory jobManagerFactory) {
                ServiceRegistry.getInstance().setJobManagerFactory(jobManagerFactory);
        }
        public void setEventBusProvider(EventBusProvider eventBusProvider) {
                ServiceRegistry.getInstance().setEventBusProvider(eventBusProvider);
        }
        public void setWebserviceStorage(WebserviceStorage webserviceStorage) {
                ServiceRegistry.getInstance().setWebserviceStorage(webserviceStorage);
        }
        public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
                ServiceRegistry.getInstance().setDatatypeRegistry(datatypeRegistry);
        }

}
