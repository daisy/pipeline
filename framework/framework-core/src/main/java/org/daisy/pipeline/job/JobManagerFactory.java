package org.daisy.pipeline.job;

import org.daisy.common.properties.PropertyPublisher;
import org.daisy.common.properties.PropertyPublisherFactory;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.job.impl.DefaultJobManager;
import org.daisy.pipeline.properties.Properties;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    name = "job-manager-factory",
    service = { JobManagerFactory.class }
)
public class JobManagerFactory {
        private JobStorage storage;
        private MessageStorage messageStorage;
        private JobExecutionService executionService;
        
        public JobManager createFor(Client client){
                return new DefaultJobManager(this.storage.filterBy(client),
                                messageStorage,
                                this.executionService.filterBy(client),
                                new JobContextFactory(client));
        }
        public JobManager createFor(Client client,JobBatchId batchId){
                return new DefaultJobManager(this.storage.filterBy(client).filterBy(batchId),
                                messageStorage,
                                this.executionService.filterBy(client),
                                new JobContextFactory(client));
        }

        /**
         * @param storage the storage to set
         */
        @Reference(
            name = "job-storage",
            unbind = "-",
            service = JobStorage.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
        )
        public void setJobStorage(JobStorage storage) {
                //TODO: check null
                this.storage = storage;
        }

        @Reference(
            name = "message-storage",
            unbind = "-",
            service = MessageStorage.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
        )
        public void setMessageStorage(MessageStorage storage) {
                this.messageStorage = storage;
        }

        /**
         * @param executionService the executionService to set
         */
        @Reference(
            name = "execution-service",
            unbind = "-",
            service = JobExecutionService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
        )
        public void setExecutionService(JobExecutionService executionService) {
                //TODO:check null
                this.executionService = executionService;
        }

        //FIXME: probably move these two methods somewhere else, maybe a dummy class for the framework just tu publish this.
        public void setPropertyPublisherFactory(PropertyPublisherFactory propertyPublisherFactory){
                PropertyPublisher propertyPublisher=propertyPublisherFactory.newPropertyPublisher();    
                //the property publishing step goes here
                propertyPublisher.publish("org.daisy.pipeline.iobase" ,Properties.getProperty("org.daisy.pipeline.iobase","" ),this.getClass());
                propertyPublisher.publish("org.daisy.pipeline.home" ,Properties.getProperty("org.daisy.pipeline.home","" ),this.getClass());
                propertyPublisher.publish("org.daisy.pipeline.logdir",Properties.getProperty("org.daisy.pipeline.logdir","" ),this.getClass());
                propertyPublisher.publish("org.daisy.pipeline.procs",Properties.getProperty("org.daisy.pipeline.procs","" ),this.getClass());
        }

        public void unsetPropertyPublisherFactory(PropertyPublisherFactory propertyPublisherFactory){
                PropertyPublisher propertyPublisher=propertyPublisherFactory.newPropertyPublisher();    
                //the property unpublishing step goes here
                propertyPublisher.unpublish("org.daisy.pipeline.iobase" ,  this.getClass());
                propertyPublisher.unpublish("org.daisy.pipeline.home"   ,  this.getClass());
                propertyPublisher.unpublish("org.daisy.pipeline.logdir" ,  this.getClass());

        }
}
