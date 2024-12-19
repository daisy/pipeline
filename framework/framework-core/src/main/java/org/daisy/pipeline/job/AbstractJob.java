package org.daisy.pipeline.job;

import java.io.File;
import java.net.URI;
import java.util.function.Consumer;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.daisy.pipeline.script.Script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.MDC;

/**
 * The Class Job defines the execution unit.
 */
public abstract class AbstractJob implements Job {

        protected static final Logger logger = LoggerFactory.getLogger(Job.class);

        private volatile Status status = Status.IDLE;
        protected Priority priority; // used in PersistentJob
        protected AbstractJobContext ctxt; // used in PersistentJob
        private final boolean managed;
        private boolean closed = false;

        /**
         * @param managed Whether the Job will be managed by a {@link JobManager}.
         */
        protected AbstractJob(AbstractJobContext ctxt, Priority priority, boolean managed) {
                this.ctxt = ctxt;
                this.priority = priority != null ? priority : Priority.MEDIUM;
                this.managed = managed;
        }

        // for use in VolatileJob
        protected AbstractJob(AbstractJob job) {
                this(job.ctxt, job);
        }

        // for use in PersistentJob
        protected AbstractJob(AbstractJobContext ctxt, AbstractJob job) {
                this(ctxt, job.priority, job.managed);
        }

        @Override
        public JobId getId() {
                return ctxt.getId();
        }

        @Override
        public String getNiceName() {
                assertOpen();
                return ctxt.getName();
        }

        @Override
        public Script getScript() {
                assertOpen();
                return ctxt.getScript();
        }

        @Override
        public Status getStatus() {
                assertOpen();
                return status;
        }

        // for subclasses
        protected synchronized void setStatus(Status status) {
                this.status = status;
        }

        @Override
        public JobMonitor getMonitor() {
                assertOpen();
                return ctxt.getMonitor();
        }

        @Override
        public URI getLogFile() {
                assertOpen();
                return ctxt.getLogFile();
        }

        @Override
        public JobResultSet getResults() {
                assertOpen();
                return ctxt.getResults();
        }

        @Override
        public JobBatchId getBatchId() {
                assertOpen();
                return ctxt.getBatchId();
        }

        @Override
        public Client getClient() {
                assertOpen();
                return ctxt.getClient();
        }

        public Priority getPriority() {
                return priority;
        }

        public AbstractJobContext getContext() {
                return ctxt;
        }

        public synchronized final void changeStatus(Status to) {
                logger.info(String.format("Changing job status to: %s", to));
                status = to;
                onStatusChanged();
                if (ctxt.statusListeners != null) {
                        synchronized (ctxt.statusListeners) {
                                for (Consumer<Job.Status> listener : ctxt.statusListeners)
                                        listener.accept(status);
                        }
                }
        }

        // see  ch.qos.logback.classic.ClassicConstants
        private static final Marker FINALIZE_SESSION_MARKER = MarkerFactory.getMarker("FINALIZE_SESSION");

        private boolean run = false;

        @Override
        public final void run() {
                if (managed)
                        throw new UnsupportedOperationException("Managed job can only be run by the JobManager");
                managedRun();
        }

        public synchronized void managedRun() {
                assertOpen();
                if (run)
                        throw new UnsupportedOperationException("Can not run a job more than once");
                else
                        run = true;
                Script script = ctxt.getScript();

                // used in JobLogFileAppender
                MDC.put("jobid", getId().toString());
                logger.info("Starting to log to job's log file: " + getId().toString());

                changeStatus(Status.RUNNING);
                if (ctxt.messageBus == null || ctxt.properties == null)
                        // This means we've tried to execute a PersistentJob that was read from the
                        // database. This should not happen because upon creation jobs are
                        // immediately submitted to DefaultJobExecutionService, which keeps them in
                        // memory, and old idle jobs (created but not executed before a shutdown)
                        // are not added to the execution queue upon launching Pipeline.
                        throw new IllegalStateException();
                try {
                        JobResultSet.Builder resultBuilder = newResultSetBuilder(script);
                        Job.Status status = script.run(ctxt.input, ctxt.properties, ctxt.messageBus, resultBuilder, ctxt.resultDir);
                        ctxt.results = resultBuilder.build();
                        onResultsChanged();
                        changeStatus(status);
                } catch (OutOfMemoryError e) {
                        changeStatus( Status.ERROR);
                        ctxt.messageBus.append(new MessageBuilder()
                                               .withLevel(Level.ERROR)
                                               .withText(e.getMessage()))
                                       .close();
                        logger.error("job consumed all heap space", e);
                } catch (Throwable e) {
                        changeStatus( Status.ERROR);
                        ctxt.messageBus.append(new MessageBuilder()
                                               .withLevel(Level.ERROR)
                                               .withText(e.getMessage() + " (Please see detailed log for more info.)"))
                                  .close();
                        if (e instanceof XProcErrorException) {
                                logger.error("job finished with error state\n" + e.toString());
                                logger.debug("job finished with error state", e);
                        } else
                                logger.error("job finished with error state", e);
                }

                logger.info(FINALIZE_SESSION_MARKER,"Stopping logging to job's log file");
                MDC.remove("jobid");
        }

        @Override
        public final void close() {
                if (managed)
                        throw new UnsupportedOperationException("Managed job can only be closed by the JobManager");
                managedClose();
        }

        public synchronized void managedClose() {
                if (!closed) {
                        logger.info(String.format("Deleting files for job %s", getId()));
                        JobURIUtils.deleteJobBaseDir(getId().toString());
                        closed = true;
                }
        }

        private void assertOpen() {
                if (closed)
                        throw new UnsupportedOperationException("Job is closed");
        }

        // for subclasses
        protected void onStatusChanged() {}

        // for subclasses
        protected void onResultsChanged() {}

        @Override
        public boolean equals(Object object) {
                return (object instanceof Job)   && 
                        this.getId().equals(((Job) object).getId());
        }

        protected JobResultSet.Builder newResultSetBuilder(Script script) {
                return new JobResultSet.Builder(script);
        }
}
