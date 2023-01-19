package org.daisy.pipeline.job.impl;

import java.util.List;

import org.daisy.common.xproc.XProcEngine;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.job.JobQueue;
import org.daisy.pipeline.job.JobStorage;
import org.daisy.pipeline.script.BoundXProcScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * DefaultJobManager allows to manage the jobs submitted to the daisy pipeline 2
 */
public class DefaultJobManager implements JobManager {

        private static final Logger logger = LoggerFactory
                        .getLogger(DefaultJobManager.class);

        private final Client client;
        private final JobMonitorFactory monitorFactory;
        private final XProcEngine xprocEngine;
        private final JobStorage storage;
        private final JobExecutionService executionService;

        /**
         * @param storage
         * @param executionService
         */
        public DefaultJobManager(Client client,
                                 JobMonitorFactory monitorFactory,
                                 XProcEngine xprocEngine,
                                 JobStorage storage,
                                 JobExecutionService executionService) {
                this.client = client;
                this.monitorFactory = monitorFactory;
                this.xprocEngine = xprocEngine;
                this.storage = storage;
                this.executionService = executionService;
        }

        @SuppressWarnings("unchecked") // safe cast to Iterable<Job>
        @Override
        public Iterable<Job> getJobs() {
                return (Iterable<Job>)(Iterable<?>)this.storage;
        }

        /**
         * Deletes the job and cleans its context. If you are not using AbstractJobContexts
         * (you have implemented your own JobContexts) you should implement this class and
         * make a custom deletion, otherwise the context won't be cleared out.
         * @see org.daisy.pipeline.job.JobManager#deleteJob(org.daisy.pipeline.job.JobId)
         */
        @SuppressWarnings("unchecked") // safe cast to Optional<Job>
        @Override
        public Optional<Job> deleteJob(JobId id) {
                Optional<AbstractJob> job = this.storage.get(id);
                if(!job.isPresent()){
                        return Optional.absent();
                }
                this.storage.remove(id);
                job.get().managedClose();
                return (Optional<Job>)(Optional<?>)job;
        }

        @SuppressWarnings("unchecked") // safe cast to Optional<Job>
        @Override
        public Optional<Job> getJob(JobId id) {
                Optional<AbstractJob> job = this.storage.get(id);
                return (Optional<Job>)(Optional<?>)job;
        }

        @SuppressWarnings("unchecked") // safe cast to Iterable<Job>
        @Override
        public Iterable<Job> deleteAll() {
                logger.info("deleting all jobs");
                List<AbstractJob> jobs = Lists.newLinkedList();
                Iterables.addAll(jobs, storage);
                //iterate over a copy of the jobs, to make sure
                //that we clean the context up
                for (AbstractJob job : jobs) {
                        logger.debug(String.format("Deleting job %s", job));
                        job.managedClose();
                        this.storage.remove(job.getId());
                }
                return (Iterable<Job>)(Iterable<?>)jobs;
        }

        @Override
        public JobManager.JobBuilder newJob(BoundXProcScript boundScript) {
                return new DefaultJobBuilder(monitorFactory,
                                             xprocEngine,
                                             client,
                                             boundScript,
                                             true) {
                        @SuppressWarnings("unchecked") // safe cast to Optional<Job>
                        @Override
                        public Optional<Job> build() {
                                // store it
                                Optional<AbstractJob> job = storage.add(
                                        (AbstractJob)super.build().get() // DefaultJobBuilder.build() creates VolatileJob
                                );
                                if (job.isPresent()) {
                                        // broadcast status
                                        job.get().changeStatus(Status.IDLE);
                                        // execute it
                                        executionService.submit(job.get());
                                }
                                return (Optional<Job>)(Optional<?>)job;
                        }
                };
        }

        @Override
        public JobQueue getExecutionQueue() {
                return this.executionService.getQueue();
        }
}
