package org.daisy.pipeline.nonpersistent.impl.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.daisy.common.priority.Priority;
import org.daisy.common.properties.Properties;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
    name = "volatile-job-storage",
    service = { JobStorage.class }
)
public class VolatileJobStorage implements JobStorage {

        private static final boolean VOLATILE_DISABLED = "true".equalsIgnoreCase(
                Properties.getProperty("org.daisy.pipeline.persistence"));
        private static final Logger logger = LoggerFactory
                        .getLogger(VolatileJobStorage.class);
        private Map<JobId,AbstractJob> jobs = Collections
                        .synchronizedMap(new HashMap<JobId,AbstractJob>());
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

        VolatileJobStorage(Map<JobId,AbstractJob> jobs, Predicate<Job> filter) {
                this.jobs = jobs;
                this.filter = filter;
        }

        @Override
        public Iterator<AbstractJob> iterator() {
                return Collections2.filter(this.jobs.values(),this.filter).iterator();
        }

        @Override
        public synchronized Optional<AbstractJob> add(final Priority priority, final AbstractJobContext ctxt) {
                if (!this.jobs.containsKey(ctxt.getId()))
                        return Optional.of(new VolatileJob(ctxt, priority));
                return Optional.absent();
        }

        @Override
        public synchronized Optional<AbstractJob> remove(JobId jobId) {
                Optional<AbstractJob> job = this.get(jobId);
                if(job.isPresent()){
                        this.jobs.remove(jobId);
                }
                return job;

        }

        @Override
        public synchronized Optional<AbstractJob> get(JobId jobId) {
                AbstractJob job=this.jobs.get(jobId);
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
                return new VolatileJobStorage(jobs, Predicates.and(this.filter, new Predicate<Job>() {

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
                        return new VolatileJobStorage(jobs, Predicates.and(this.filter, new Predicate<Job>() {

                                @Override
                                public boolean apply(Job job) {
                                        //check if the client id is the one we're filtering by
                                        return job.getContext().getClient().getId().equals(client.getId());
                                }
                        }));
                }
        }

        private class VolatileJob extends AbstractJob {
                VolatileJob(AbstractJobContext ctxt, Priority priority) {
                        super(ctxt, priority);
                        // Store the job before broadcasting its status
                        VolatileJobStorage.this.jobs.put(ctxt.getId(), this);
                        changeStatus(Status.IDLE);
                }
        }
}
