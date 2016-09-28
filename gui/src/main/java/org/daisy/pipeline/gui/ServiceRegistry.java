package org.daisy.pipeline.gui;

import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Monitor;

public class ServiceRegistry{
        private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);


        private  ScriptRegistry scriptRegistry = null;
        private  JobManagerFactory jobManagerFactory = null;
        private  EventBusProvider eventBusProvider = null;
        private WebserviceStorage webserviceStorage = null;

        private static ServiceRegistry instance=null;

        private final Monitor monitor = new Monitor();
        private final Monitor.Guard pipelineServicesAvailable = new Monitor.Guard(monitor) {
                public boolean isSatisfied() {
                        return instance!=null &&
                                ServiceRegistry.this.scriptRegistry!=null &&
                                ServiceRegistry.this.jobManagerFactory!=null &&
                                ServiceRegistry.this.eventBusProvider!=null &&
                                ServiceRegistry.this.webserviceStorage!=null
                                ;
                }
        };

        private GUIService guiService;

        private ServiceRegistry() {
        }

        static ServiceRegistry getInstance(){
                if (instance==null){
                        instance=new ServiceRegistry();

                }
                return instance;
        }
        
        public void notifyReady(PipelineApplication app)throws InterruptedException{
                try{
                        monitor.enterWhen(this.pipelineServicesAvailable);
                        logger.debug("setting serviceregistry");
                        app.setServiceRegistry(this);
                }catch (InterruptedException ie){
                        throw ie;
                }finally{
                        monitor.leave();
                }


        }

        /**
         * @return the scriptRegistry
         */
        public ScriptRegistry getScriptRegistry() {
                return scriptRegistry;
        }

        /**
         * @param scriptRegistry the scriptRegistry to set
         */
        public void setScriptRegistry(ScriptRegistry scriptRegistry) {
                this.monitor.enter();
                this.scriptRegistry = scriptRegistry;
                this.monitor.leave();
        }

        /**
         * @return the jobManagerFactory
         */
        public JobManagerFactory getJobManagerFactory() {
                return jobManagerFactory;
        }

        /**
         * @param jobManagerFactory the jobManagerFactory to set
         */
        public void setJobManagerFactory(JobManagerFactory jobManagerFactory) {
                this.monitor.enter();
                this.jobManagerFactory = jobManagerFactory;
                this.monitor.leave();
        }

        /**
         * @return the eventBusProvider
         */
        public EventBusProvider getEventBusProvider() {
                return eventBusProvider;
        }

        /**
         * @param eventBusProvider the eventBusProvider to set
         */
        public void setEventBusProvider(EventBusProvider eventBusProvider) {
                this.monitor.enter();
                this.eventBusProvider = eventBusProvider;
                this.monitor.leave();
        }

        /**
         * @return the webserviceStorage
         */
        public WebserviceStorage getWebserviceStorage() {
                return webserviceStorage;
        }

        /**
         * @param webserviceStorage the webserviceStorage to set
         */
        public void setWebserviceStorage(WebserviceStorage webserviceStorage) {
                this.monitor.enter();
                this.webserviceStorage = webserviceStorage;
                this.monitor.leave();
        }

        public void setGUIService(GUIService guiService) {
                this.monitor.enter();
                this.guiService=guiService;
                this.monitor.leave();
        }

        public GUIService getGUIService(){
                return this.guiService;
        }

        
}
