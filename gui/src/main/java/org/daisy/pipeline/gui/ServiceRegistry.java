package org.daisy.pipeline.gui;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

import com.google.common.util.concurrent.Monitor;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This is a hack to make OSGi services available to objects that are not instantiated by
 * the OSGi framework, such as PipelineApplication.
 */
public class ServiceRegistry {

        private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

        private static ServiceRegistry instance = null;
        
        private ServiceRegistry() {
        }

        static ServiceRegistry getInstance() {
                if (instance == null)
                        instance = new ServiceRegistry();
                return instance;
        }

        private ScriptRegistry scriptRegistry = null;
        private JobManager jobManager = null;
        private JobManagerFactory jobManagerFactory = null;
        private EventBusProvider eventBusProvider = null;
        private List<Object> eventBusListeners = null;
        private WebserviceStorage webserviceStorage = null;
        private Client client = null;
        private DatatypeRegistry datatypeRegistry = null;
        private GUIService guiService = null;

        private final Monitor monitor = new Monitor();
        private final Monitor.Guard servicesAvailable = new Monitor.Guard(monitor) {
                public boolean isSatisfied() {
                        return instance != null &&
                                ServiceRegistry.this.scriptRegistry != null &&
                                ServiceRegistry.this.jobManagerFactory != null &&
                                ServiceRegistry.this.eventBusProvider != null &&
                                ServiceRegistry.this.webserviceStorage != null &&
                                ServiceRegistry.this.datatypeRegistry != null &&
                                ServiceRegistry.this.guiService != null;
                }
        };

        public boolean isReady() {
                return servicesAvailable.isSatisfied();
        }
        
        public void waitUntilReady() throws InterruptedException {
                waitUntilReady(-1);
        }
        
        public boolean waitUntilReady(long timeout) throws InterruptedException {
                try {
                        boolean ready;
                        if (timeout >= 0)
                                ready = monitor.enterWhen(this.servicesAvailable, timeout, TimeUnit.MILLISECONDS);
                        else {
                                monitor.enterWhen(this.servicesAvailable);
                                ready = true;
                        }
                        logger.debug("setting serviceregistry");
                        return ready;
                } catch (InterruptedException ie) {
                        throw ie;
                } finally {
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
         * @return the jobManager
         */
        public JobManager getJobManager() {
                if (jobManagerFactory != null && getClient() != null) {
                        if (jobManager == null)
                                jobManager = jobManagerFactory.createFor(getClient());
                        return jobManager;
                } else
                        return null;
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
        public synchronized void registerEventBusListener(Object listener) {
                if (eventBusListeners == null)
                        eventBusListeners = new ArrayList<Object>();
                eventBusListeners.add(listener);
                if (eventBusProvider != null)
                        eventBusProvider.get().register(listener);
        }

        /**
         * @param eventBusProvider the eventBusProvider to set
         */
        public synchronized void setEventBusProvider(EventBusProvider eventBusProvider) {
                this.monitor.enter();
                if (this.eventBusProvider != null && eventBusListeners != null)
                        for (Object l : eventBusListeners)
                                this.eventBusProvider.get().unregister(l);
                this.eventBusProvider = eventBusProvider;
                if (eventBusProvider != null && eventBusListeners != null)
                        for (Object l : eventBusListeners)
                                this.eventBusProvider.get().register(l);
                this.monitor.leave();
        }

        private Client getClient() {
                if (webserviceStorage != null) {
                        if (client == null)
                                client = webserviceStorage.getClientStorage().defaultClient();
                        return client;
                } else
                        return null;
        }

        /**
         * @param webserviceStorage the webserviceStorage to set
         */
        public void setWebserviceStorage(WebserviceStorage webserviceStorage) {
                this.monitor.enter();
                this.webserviceStorage = webserviceStorage;
                this.monitor.leave();
        }

        /**
         * @return the datatypeRegistry
         */
        public DatatypeRegistry getDatatypeRegistry() {
                return datatypeRegistry;
        }

        /**
         * @param datatypeRegistry the datatypeRegistry to set
         */
        public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
                this.monitor.enter();
                this.datatypeRegistry = datatypeRegistry;
                this.monitor.leave();
        }

        public void setGUIService(GUIService guiService) {
                this.monitor.enter();
                this.guiService = guiService;
                this.monitor.leave();
        }

        public GUIService getGUIService() {
                return this.guiService;
        }

        @Component
        public static class ServiceBinder {

                private ServiceRegistry registry = ServiceRegistry.getInstance();

                @Reference(
                        name = "script-registry",
                        unbind = "unsetScriptRegistry",
                        service = ScriptRegistry.class,
                        cardinality = ReferenceCardinality.MANDATORY,
                        policy = ReferencePolicy.DYNAMIC
                )
                public void setScriptRegistry(ScriptRegistry scriptRegistry) {
                        registry.setScriptRegistry(scriptRegistry);
                }
                public void unsetScriptRegistry(ScriptRegistry scriptRegistry) {
                        registry.setScriptRegistry(null);
                }

                @Reference(
                        name = "job-manager-factory",
                        unbind = "unsetJobManagerFactory",
                        service = JobManagerFactory.class,
                        cardinality = ReferenceCardinality.MANDATORY,
                        policy = ReferencePolicy.DYNAMIC
                )
                public void setJobManagerFactory(JobManagerFactory jobManagerFactory) {
                        registry.setJobManagerFactory(jobManagerFactory);
                }
                public void unsetJobManagerFactory(JobManagerFactory jobManagerFactory) {
                        registry.setJobManagerFactory(null);
                }

                @Reference(
                        name = "event-bus-provider",
                        unbind = "unsetEventBusProvider",
                        service = EventBusProvider.class,
                        cardinality = ReferenceCardinality.MANDATORY,
                        policy = ReferencePolicy.DYNAMIC
                )
                public void setEventBusProvider(EventBusProvider eventBusProvider) {
                        registry.setEventBusProvider(eventBusProvider);
                }
                public void unsetEventBusProvider(EventBusProvider eventBusProvider) {
                        registry.setEventBusProvider(null);
                }

                @Reference(
                        name = "webservice-storage",
                        unbind = "unsetWebserviceStorage",
                        service = WebserviceStorage.class,
                        cardinality = ReferenceCardinality.MANDATORY,
                        policy = ReferencePolicy.DYNAMIC
                )
                public void setWebserviceStorage(WebserviceStorage webserviceStorage) {
                        registry.setWebserviceStorage(webserviceStorage);
                }
                public void unsetWebserviceStorage(WebserviceStorage webserviceStorage) {
                        registry.setWebserviceStorage(null);
                }

                @Reference(
                        name = "datatype-registry",
                        unbind = "unsetDatatypeRegistry",
                        service = DatatypeRegistry.class,
                        cardinality = ReferenceCardinality.MANDATORY,
                        policy = ReferencePolicy.DYNAMIC
                )
                public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
                        registry.setDatatypeRegistry(datatypeRegistry);
                }
                public void unsetDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
                        registry.setDatatypeRegistry(null);
                }
        }
}
