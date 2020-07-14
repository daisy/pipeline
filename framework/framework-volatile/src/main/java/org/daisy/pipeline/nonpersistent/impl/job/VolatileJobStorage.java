package org.daisy.pipeline.nonpersistent.impl.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobStorage;
import org.daisy.pipeline.job.RuntimeConfigurator;
import org.daisy.pipeline.properties.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    name = "volatile-job-storage",
    service = { JobStorage.class }
)
public class VolatileJobStorage implements JobStorage {

        private static final boolean VOLATILE_DISABLED = "true".equalsIgnoreCase(
                Properties.getProperty("org.daisy.pipeline.persistence"));
        private static final Logger logger = LoggerFactory
                        .getLogger(VolatileJobStorage.class);
        private Map<JobId, Job> jobs = Collections
                        .synchronizedMap(new HashMap<JobId, Job>());
        private RuntimeConfigurator configurator;

        private Predicate<Job> filter = Predicates.alwaysTrue();

        public VolatileJobStorage(){}

        /**
         * @throws RuntimeException if volatile storage is disabled through the org.daisy.pipeline.persistence system property.
         */
        @Activate
        public void activate() {
                if (VOLATILE_DISABLED)
                        throw new RuntimeException("Volatile storage is disabled");
        }

        /**
         * @param bus
         * @param filter
         */
        public VolatileJobStorage(Map<JobId, Job> jobs, RuntimeConfigurator configurator, Predicate<Job> filter) {
                this.jobs=jobs;
                this.configurator = configurator;
                this.filter = filter;
        }

        @Reference(
           name = "runtime-configurator",
           unbind = "-",
           service = RuntimeConfigurator.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setConfigurator(RuntimeConfigurator configurator) {
                this.configurator = configurator;
        }

        @Override
        public Iterator<Job> iterator() {
                return Collections2.filter(this.jobs.values(),this.filter).iterator();
        }

        @Override
        public synchronized Optional<Job> add(final Priority priority, final JobContext ctxt) {

                if (!this.jobs.containsKey(ctxt.getId())) {
                        //Store the job before its status gets broadcasted
                        Job job = new Job.JobBuilder().withPriority(priority)
                                        .withContext(ctxt).build(new Function<Job, Job>() {
                                                @Override
                                                public Job apply(Job job) {
                                                        VolatileJobStorage.this.configurator.configure(job);
                                                        VolatileJobStorage.this.jobs.put(ctxt.getId(), job);
                                                        return job;
                                                }
                                        });
                        return Optional.of(job);
                }
                return Optional.absent();
        }

        @Override
        public synchronized Optional<Job> remove(JobId jobId) {
                Optional<Job> job = this.get(jobId);
                if(job.isPresent()){
                        this.jobs.remove(jobId);
                }
                return job;

        }

        @Override
        public synchronized Optional<Job> get(JobId jobId) {
                Job job=this.jobs.get(jobId);
                if (job==null){
                        return Optional.absent();
                }
                if(this.filter.apply(job)){
                        return Optional.fromNullable(job);
                }
                return Optional.absent();
        }

        @Override
        public JobStorage filterBy(final JobBatchId batchId) {
                return new VolatileJobStorage(this.jobs,this.configurator,Predicates.and(this.filter, new Predicate<Job>(){

                        @Override
                        public boolean apply(Job job) {
                                JobBatchId bId=job.getContext().getBatchId();
                                //check if the client id is the one we're filtering by
                                return bId!=null && bId.toString().equals(batchId.toString());
                        }
                }));
        }
        @Override
        public JobStorage filterBy(final Client client) {
                if (client.getRole().equals(Role.ADMIN)){
                        return this;
                }else{
                        return new VolatileJobStorage(this.jobs,this.configurator,Predicates.and(this.filter, new Predicate<Job>(){

                                @Override
                                public boolean apply(Job job) {
                                        //check if the client id is the one we're filtering by
                                        return job.getContext().getClient().getId().equals(client.getId());
                                }
                        }));
                }
        }
        
}
