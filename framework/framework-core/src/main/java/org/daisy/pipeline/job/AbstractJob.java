package org.daisy.pipeline.job;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.transform.Result;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.impl.DynamicResultProvider;
import org.daisy.pipeline.job.impl.IOHelper;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.daisy.pipeline.job.impl.JobUtils;
import org.daisy.pipeline.job.impl.URITranslatorHelper;
import org.daisy.pipeline.job.impl.XProcDecorator;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;

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

        protected volatile Status status = Status.IDLE;
        protected Priority priority;
        protected AbstractJobContext ctxt;
        public final XProcEngine xprocEngine;
        private final boolean managed;
        private boolean closed = false;

        /**
         * @param managed Whether the Job will be managed by a {@link JobManager}.
         */
        protected AbstractJob(AbstractJobContext ctxt, Priority priority, XProcEngine xprocEngine, boolean managed) {
                this.ctxt = ctxt;
                this.priority = priority != null ? priority : Priority.MEDIUM;
                this.xprocEngine = xprocEngine;
                this.managed = managed;
                if (!managed)
                        changeStatus(Status.IDLE);
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
        public XProcScript getScript() {
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

                // used in JobLogFileAppender
                MDC.put("jobid", getId().toString());
                logger.info("Starting to log to job's log file: " + getId().toString());

                changeStatus(Status.RUNNING);
                XProcPipeline pipeline = null;
                if (ctxt.messageBus == null || xprocEngine == null || ctxt.output == null)
                        // This means we've tried to execute a PersistentJob that was read from the
                        // database. This should not happen because upon creation jobs are
                        // immediately submitted to DefaultJobExecutionService, which keeps them in
                        // memory, and old idle jobs (created but not executed before a shutdown)
                        // are not added to the execution queue upon launching Pipeline.
                        throw new IllegalStateException();
                try {
                        pipeline = xprocEngine.load(this.ctxt.getScript().getXProcPipelineInfo().getURI());
                        XProcResult result = pipeline.run(ctxt.input, () -> ctxt.messageBus, null);
                        result.writeTo(ctxt.output); // writes to files and/or streams specified in output
                        ctxt.results = buildResultSet();
                        onResultsChanged();
                        if (JobUtils.checkStatusPort(ctxt.script, ctxt.output))
                                changeStatus(Status.SUCCESS);
                        else
                                changeStatus(Status.FAIL);
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

        private JobResultSet buildResultSet() {
                return buildResultSet(ctxt.script, ctxt.input, ctxt.output, ctxt.resultMapper, newResultSetBuilder());
        }

        protected JobResultSet.Builder newResultSetBuilder() {
                return new JobResultSet.Builder();
        }

        // package private for unit tests
        static JobResultSet buildResultSet(XProcScript script, XProcInput inputs, XProcOutput outputs, URIMapper mapper) {
                return buildResultSet(script, inputs, outputs, mapper, new JobResultSet.Builder());
        }

        private static JobResultSet buildResultSet(XProcScript script, XProcInput inputs, XProcOutput outputs, URIMapper mapper, JobResultSet.Builder builder) {

                // iterate over output ports
                for (XProcPortInfo info : script.getXProcPipelineInfo().getOutputPorts()) {
                        Supplier<Result> provider = outputs.getResultProvider(info.getName());
                        if (provider == null)
                                continue;
                        String mediaType = script.getPortMetadata(info.getName()).getMediaType();
                        if (!XProcPortMetadata.MEDIA_TYPE_STATUS_XML.equals(mediaType)) {
                                if (!(provider instanceof DynamicResultProvider))
                                        // XProcDecorator makes sure this can not happen
                                        throw new IllegalArgumentException("Result supplier is expected to be a DynamicResultProvider but got: " + provider);
                                for (Result result : ((DynamicResultProvider)provider).providedResults()) {
                                        // The result was previously written to the output by
                                        // XProcResult.writeTo(XProcOutput). If the output was a file, the system ID is
                                        // the file path. If the output was a stream, the system ID may be null.
                                        String sysId = result.getSystemId();
                                        URI path = sysId == null ? null : URI.create(sysId);
                                        builder = builder.addResult(info.getName(), path == null ? null : mapper.unmapOutput(path).toString(), path, mediaType);
                                }
                        }
                }

                // iterate over output options
                for (XProcOptionInfo option : Iterables.filter(script.getXProcPipelineInfo().getOptions(),
                                                               URITranslatorHelper.getResultOptionsFilter(script))) {
                        if (inputs.getOptions().get(option.getName()) == null)
                                continue;
                        String mediaType = script.getOptionMetadata(option.getName()).getMediaType();
                        if (XProcDecorator.TranslatableOption.ANY_FILE_URI.getName().equals(script.getOptionMetadata(option.getName()).getType())) {
                                URI path; {
                                        Object val = inputs.getOptions().get(option.getName());
                                        try {
                                                path = URI.create((String)val);
                                        } catch (ClassCastException e) {
                                                throw new RuntimeException("Expected string value for option " + option.getName() + " but got: " + val.getClass());
                                        }
                                }
                                builder = builder.addResult(option.getName(), path == null ? null : mapper.unmapOutput(path).toString(), path, mediaType);
                        } else if (XProcDecorator.TranslatableOption.ANY_DIR_URI.getName().equals(script.getOptionMetadata(option.getName()).getType())) {
                                String dir; {
                                        Object val = inputs.getOptions().get(option.getName());
                                        try {
                                                dir = (String)val;
                                        } catch (ClassCastException e) {
                                                throw new RuntimeException("Expected string value for option " + option.getName() + " but got: " + val.getClass());
                                        }
                                }
                                // scan the directory to get all files inside
                                List<URI> ls = IOHelper.treeFileList(URI.create(dir));
                                for (URI path : ls) {
                                        builder = builder.addResult(option.getName(), path == null ? null : mapper.unmapOutput(path).toString(), path, mediaType);
                                }
                        }
                }
                return builder.build();
        }
}
