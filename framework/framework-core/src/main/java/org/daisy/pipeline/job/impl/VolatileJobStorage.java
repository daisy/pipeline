package org.daisy.pipeline.job.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.event.impl.VolatileMessageStorage;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

public class VolatileJobStorage implements JobStorage {

        private static final Logger logger = LoggerFactory
                        .getLogger(VolatileJobStorage.class);
        private Map<JobId,AbstractJob> jobs = Collections
                        .synchronizedMap(new HashMap<JobId,AbstractJob>());
        private Predicate<AbstractJob> filter = Predicates.alwaysTrue();
        private final MessageStorage messageStorage;

        public VolatileJobStorage() {
                messageStorage = new VolatileMessageStorage();
        }

        private VolatileJobStorage(Map<JobId,AbstractJob> jobs, MessageStorage messageStorage, Predicate<AbstractJob> filter) {
                this.jobs = jobs;
                this.messageStorage = messageStorage;
                this.filter = filter;
        }

        @Override
        public Iterator<AbstractJob> iterator() {
                return Collections2.filter(this.jobs.values(),this.filter).iterator();
        }

        @Override
        public synchronized Optional<AbstractJob> add(AbstractJob job) {
                if (!jobs.containsKey(job.getId())) {
                        job = new VolatileJob(job.getContext(), job.getPriority(), job.xprocEngine, true);
                        jobs.put(job.getId(), job);
                        return Optional.of(job);
                }
                return Optional.absent();
        }

        @Override
        public synchronized Optional<AbstractJob> remove(JobId jobId) {
                Optional<AbstractJob> job = this.get(jobId);
                if(job.isPresent()){
                        this.jobs.remove(jobId);
                }
                messageStorage.remove(jobId.toString());
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
                return new VolatileJobStorage(jobs, messageStorage, Predicates.and(this.filter, new Predicate<AbstractJob>() {
                        @Override
                        public boolean apply(AbstractJob job) {
                                JobBatchId bId = job.getContext().getBatchId();
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
                        return new VolatileJobStorage(jobs, messageStorage, Predicates.and(this.filter, new Predicate<AbstractJob>() {
                                @Override
                                public boolean apply(AbstractJob job) {
                                        //check if the client id is the one we're filtering by
                                        return job.getContext().getClient().getId().equals(client.getId());
                                }
                        }));
                }
        }

        @Override
        public MessageStorage getMessageStorage() {
                return messageStorage;
        }
}
