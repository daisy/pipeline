package org.daisy.pipeline.job.impl;

import java.util.List;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobContextFactory;
import org.daisy.pipeline.job.JobExecutionService;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobQueue;
import org.daisy.pipeline.job.JobResources;
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

        private JobStorage storage;
        private JobExecutionService executionService;
        private JobContextFactory jobContextFactory;

        /**
         * @param storage
         * @param executionService
         */
        public DefaultJobManager(JobStorage storage,
                        JobExecutionService executionService,
                        JobContextFactory jobContextFactory) {
                //check nullities
                this.storage = storage;
                this.executionService = executionService;
                this.jobContextFactory= jobContextFactory;
        }

        protected Optional<Job> newJob(Priority priority, JobContext ctxt) {
                //store it
                Optional<Job> job = this.storage.add(priority,ctxt);
                if(job.isPresent()){
                        //execute it
                        executionService.submit(job.get());
                }
                return job;
        }


        /**
         * This method allows to do some after job creation hook-ups if needed.
         */
        //protected abstract void onNewJob(Job job);

        /* (non-Javadoc)
         * @see org.daisy.pipeline.job.JobManager#getJobIds()
         */
        @Override
        public Iterable<Job> getJobs() {
                return this.storage;
        }

        /**
         * Deletes the job and cleans its context. If you are not using AbstractJobContexts
         * (you have implemented your own JobContexts) you should implement this class and
         * make a custom deletion, otherwise the context won't be cleared out.
         * @see org.daisy.pipeline.job.JobManager#deleteJob(org.daisy.pipeline.job.JobId)
         */
        @Override
        public Optional<Job> deleteJob(JobId id) {
                Optional<Job> job = this.getJob(id);
                if(!job.isPresent()){
                        return Optional.absent();
                }
                this.storage.remove(id);
                if ( job.get().getContext() instanceof AbstractJobContext) {
                        //clean the context
                        ((AbstractJobContext) job.get().getContext()).cleanUp();
                }
                return job;
        }

        /* (non-Javadoc)
         * @see org.daisy.pipeline.job.JobManager#getJob(org.daisy.pipeline.job.JobId)
         */
        @Override
        public Optional<Job> getJob(JobId id) {
                return this.storage.get(id);
        }

        @Override
        public Iterable<Job> deleteAll() {
                logger.info("deleting all jobs");
                List<Job> jobs=Lists.newLinkedList();
                Iterables.addAll(jobs,this.getJobs());
                //iterate over a copy of the jobs, to make sure
                //that we clean the context up
                for (Job job : jobs) {
                        logger.debug(String.format("Deleting job %s", job));
                        ((AbstractJobContext) job.getContext()).cleanUp();
                        this.storage.remove(job.getId());
                }
                return jobs;

        }


        @Override
        public JobManager.JobBuilder newJob(BoundXProcScript boundScript) {
                return new DefaultJobBuilder(boundScript);
        }

        class DefaultJobBuilder implements JobBuilder{
                private BoundXProcScript script;
                private boolean isMapping;
                private JobBatchId batchId;
                private JobResources resources;
                private String niceName="";
                private Priority priority=Priority.MEDIUM;

                /**
                 *
                 */
                public DefaultJobBuilder(BoundXProcScript script) {
                        this.script=script;
                }

                /**
                 * @param isMapping the isMapping to set
                 */
                public JobBuilder isMapping(boolean isMapping) {
                        this.isMapping = isMapping;
                        return this;
                }

                /**
                 * @param resources the resources to set
                 * @return
                 */
                public JobBuilder withResources(JobResources resources) {
                        this.resources = resources;
                        return this;
                }

                /**
                 * @param niceName the niceName to set
                 * @return
                 */
                public JobBuilder withNiceName(String niceName) {
                        this.niceName = niceName;
                        return this;

                }

                public Optional<Job> build(){
                        //use the context factory
                        JobContext ctxt=DefaultJobManager.this.jobContextFactory.
                                newJobContext(this.isMapping,this.niceName,this.batchId,this.script,this.resources);
                        //send to the JobManager
                        return DefaultJobManager.this.newJob(this.priority,ctxt);
                }

                @Override
                public JobBuilder withPriority(Priority priority) {
                        this.priority=priority;
                        return this;
                }

                @Override
                public JobBuilder withBatchId(JobBatchId id) {
                        this.batchId=id;
                        return this;
                }

        }

        @Override
        public JobQueue getExecutionQueue() {
                return this.executionService.getQueue();
        }


}
