package org.daisy.pipeline.job.impl;

import org.daisy.common.priority.Prioritizable;
import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.PriorityThreadPoolExecutor;
import org.daisy.common.priority.timetracking.TimeFunctions;
import org.daisy.common.priority.timetracking.TimeTrackerFactory;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobExecutionService;
import org.daisy.pipeline.job.JobQueue;
import org.daisy.pipeline.job.impl.fuzzy.FuzzyJobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.MDC;

import com.google.common.base.Predicate;

/**
 * DefaultJobExecutionService is the defualt way to execute jobs
 */
public class DefaultJobExecutionService implements JobExecutionService {

        static final String NUM_PROCS="org.daisy.pipeline.procs";
        /** The Constant logger. */
        private static final Logger logger = LoggerFactory
                        .getLogger(DefaultJobExecutionService.class);
        /** The xproc engine. */
        private XProcEngine xprocEngine;

        private PriorityThreadPoolExecutor<Job> executor;
        private JobQueue executionQueue;
        //Get the executor configured by the system property
        static PriorityThreadPoolExecutor<Job> configureExecutor(){
                int procs=2;

                try{
                        String confProcs=System.getProperty(NUM_PROCS,"2");
                        procs=Integer.parseInt(confProcs);
                }catch(NumberFormatException e){
                        logger.info(String.format("Error parsing %s %s",NUM_PROCS,procs));
                }
                logger.info(String.format("Initialising number of processors to %s",procs));
                PriorityThreadPoolExecutor<Job> executor = PriorityThreadPoolExecutor
                        .newFixedSizeThreadPoolExecutor(
                                        procs,
                                        TimeTrackerFactory.newFactory(1,
                                                TimeFunctions.newLinearTimeFunctionFactory()));

                return executor;
        }

        public DefaultJobExecutionService(){
                this.executor=DefaultJobExecutionService.configureExecutor();
                this.executionQueue=new DefaultJobQueue(this.executor); 
        }
        /**
         * @param xprocEngine
         * @param executor
         * @param executionQueue
         */
        public DefaultJobExecutionService(XProcEngine xprocEngine,
                        PriorityThreadPoolExecutor<Job> executor, JobQueue executionQueue) {
                this.xprocEngine = xprocEngine;
                this.executor = executor;
                this.executionQueue = executionQueue;
        }

        /**
         * Sets the x proc engine.
         *
         * @param xprocEngine
         *            the new x proc engine
         */
        public void setXProcEngine(XProcEngine xprocEngine) {
                this.xprocEngine = xprocEngine;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.daisy.pipeline.job.JobExecutionService#submit(org.daisy.pipeline.
         * job.Job)
         */
        @Override
        public void submit(final Job job) {
                //logger.info("Submitting job");
                //Make the runnable ready to submit to the fuzzy-prioritized thread pool
                PrioritizableRunnable<Job> runnable = FuzzyJobFactory.newFuzzyRunnable(job,
                                this.getRunnable(job));
                //Conviniently wrap it in a PrioritizedJob for later access
                this.executor.execute(runnable);
        }

        // see  ch.qos.logback.classic.ClassicConstants
        private static final Marker FINALIZE_SESSION_MARKER = MarkerFactory.getMarker("FINALIZE_SESSION");

        Runnable getRunnable(final Job job) {
                return new ThreadWrapper(new Runnable() {

                        @Override
                        public void run() {

                                try {
                                        logger.info("Starting to log to job's log file too:"
                                                        + job.getId().toString());
                                        MDC.put("jobid", job.getId().toString());
                                        job.run(xprocEngine);
                                        logger.info(FINALIZE_SESSION_MARKER,"Stopping logging to job's log file");
                                        MDC.remove("jobid");
                                } catch (Exception e) {
                                        throw new RuntimeException(e.getCause());
                                }

                        }
                });
        }

        /**
         * This class offers a solution to avoid memory leaks due to
         * the missuse of ThreadLocal variables.
         * The actual run implementation may be a little bit naive regarding the interrupt handling
         *
         */
        private static class ThreadWrapper implements Runnable {

                private static final Logger logger = LoggerFactory
                                .getLogger(ThreadWrapper.class);
                private Runnable runnable;

                /**
                 * Constructs a new instance.
                 *
                 * @param runnable The runnable for this instance.
                 */
                public ThreadWrapper(Runnable runnable) {
                        this.runnable = runnable;
                }

                public void run() {
                        logger.info("Starting wrappedThread :"
                                        + Thread.currentThread().getName());
                        Thread t = new Thread(this.runnable);
                        t.start();
                        try {
                                t.join();
                        } catch (InterruptedException e) {
                                logger.warn("ThreadWrapper was interrupted...");
                        }
                }

        }

        protected PriorityThreadPoolExecutor<Job> getExecutor() {
                return this.executor;
        }

        
        @Override
        public JobQueue getQueue() {
                return this.executionQueue;

        }

        @Override
        public JobExecutionService filterBy(final Client client) {
                if (client.getRole()==Role.ADMIN){
                        return this;
                }else{
                        return new DefaultJobExecutionService(this.xprocEngine, this.executor, 
                                        new FilteredJobQueue(this.executor,
                                                new Predicate<Prioritizable<Job>>() {
                                                        @Override
                                                        public boolean apply(Prioritizable<Job> pJob) {
                                                                return pJob.prioritySource().getContext()
                                                .getClient().getId().equals(client.getId());
                                                        }
                                                }
                        ));
                }
        }
}
