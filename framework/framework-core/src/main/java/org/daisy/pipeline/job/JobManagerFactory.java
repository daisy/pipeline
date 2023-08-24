package org.daisy.pipeline.job;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.impl.DefaultJobBuilder;
import org.daisy.pipeline.job.impl.DefaultJobExecutionService;
import org.daisy.pipeline.job.impl.DefaultJobManager;
import org.daisy.pipeline.job.impl.JobExecutionService;
import org.daisy.pipeline.job.impl.VolatileJobStorage;
import org.daisy.pipeline.script.BoundScript;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    name = "job-manager-factory",
    service = {
            JobManagerFactory.class,
            JobFactory.class
    }
)
public class JobManagerFactory implements JobFactory {

        private static final Logger logger = LoggerFactory.getLogger(JobManagerFactory.class);

        private final static Property procsProperty = Properties.getProperty("org.daisy.pipeline.procs",
                                                                             false,
                                                                             "Maximum allowed number of jobs running simultaneously",
                                                                             false,
                                                                             "2");
        private final static Property logLevelProperty = Properties.getProperty("org.daisy.pipeline.log.level",
                                                                                true,
                                                                                "Disable job messages below this level",
                                                                                false,
                                                                                "INFO");

        @Override
        public JobFactory.JobBuilder newJob(BoundScript boundScript) {
                return new DefaultJobBuilder(JobMonitorFactory.LIVE_MONITOR_FACTORY,
                                             xprocEngine,
                                             null,
                                             boundScript,
                                             false,
                                             logLevelProperty);
        }

        /**
         * Create a job manager for all jobs.
         */
        public JobManager create() {
                return createFor(Client.DEFAULT_ADMIN);
        }

        /**
         * Create a job manager for only the jobs belonging to a certain batch.
         */
        public JobManager createFor(JobBatchId batchId) {
                return createFor(Client.DEFAULT_ADMIN, batchId);
        }

        /**
         * Create a job manager for only the jobs visible for a certain client. An admin client can
         * see all jobs, other clients can only see the jobs that they created.
         *
         * This method is primarily intended to be used by the web service. In other contexts
         * clients make less sence.
         */
        public JobManager createFor(Client client) {
                return new DefaultJobManager(client,
                                             monitorFactory,
                                             xprocEngine,
                                             storage.filterBy(client),
                                             executionService.filterBy(client),
                                             logLevelProperty);
        }

        /**
         * Create a job manager for only the jobs visible for a certain client and belonging to a
         * certain batch. An admin client can see all jobs, other clients can only see the jobs that
         * they created.
         *
         * This method is primarily intended to be used by the web service. In other contexts
         * clients make less sence.
         */
        public JobManager createFor(Client client, JobBatchId batchId) {
                return new DefaultJobManager(client,
                                             monitorFactory,
                                             xprocEngine,
                                             storage.filterBy(client).filterBy(batchId),
                                             executionService.filterBy(client),
                                             logLevelProperty);
        }

        private JobStorage storage;
        private JobMonitorFactory monitorFactory;
        private XProcEngine xprocEngine;
        private JobExecutionService executionService;

        @Activate
        protected void init() {
                if (storage == null)
                        storage = new VolatileJobStorage();
                monitorFactory = new JobMonitorFactory(storage);
                int procs = 2; {
                        String prop = procsProperty.getValue();
                        try {
                                procs = Integer.parseInt(prop);
                        } catch (NumberFormatException e) {
                                logger.info(String.format("Failed to parse property '%s': %s", procsProperty.getName(), prop));
                        }
                }
                this.executionService = new DefaultJobExecutionService(procs);
        }

        @Reference(
            name = "job-storage",
            unbind = "-",
            service = JobStorage.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.STATIC
        )
        protected void setJobStorage(JobStorage storage) {
                this.storage = storage;
        }

        @Reference(
           name = "xproc-engine",
           unbind = "-",
           service = XProcEngine.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        protected void setXProcEngine(XProcEngine xprocEngine) {
                this.xprocEngine = xprocEngine;
        }
}
