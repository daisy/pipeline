package org.daisy.pipeline.job;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;

import com.google.common.base.Supplier;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.impl.DynamicResultProvider;
import org.daisy.pipeline.job.impl.IOHelper;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.daisy.pipeline.job.impl.StatusResultProvider;
import org.daisy.pipeline.job.impl.XProcDecorator;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScript.XProcScriptOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.MDC;

import org.w3c.dom.Document;

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
                if (!(script instanceof XProcScript))
                        throw new IllegalStateException("Don't know how to run script: " + script);

                // used in JobLogFileAppender
                MDC.put("jobid", getId().toString());
                logger.info("Starting to log to job's log file: " + getId().toString());

                changeStatus(Status.RUNNING);
                XProcPipeline pipeline = null;
                if (ctxt.messageBus == null || xprocEngine == null)
                        // This means we've tried to execute a PersistentJob that was read from the
                        // database. This should not happen because upon creation jobs are
                        // immediately submitted to DefaultJobExecutionService, which keeps them in
                        // memory, and old idle jobs (created but not executed before a shutdown)
                        // are not added to the execution queue upon launching Pipeline.
                        throw new IllegalStateException();
                try {
                        pipeline = xprocEngine.load(((XProcScript)script).getURI());
                        XProcDecorator decorator = XProcDecorator.from((XProcScript)script, ctxt.uriMapper);
                        XProcInput input = decorator.decorate(ctxt.input);
                        XProcResult result = pipeline.run(input, () -> ctxt.messageBus, null);
                        XProcOutput output = decorator.decorate(new XProcOutput.Builder().build());
                        result.writeTo(output); // writes to files and/or streams specified in output
                        ctxt.results = buildResultSet((XProcScript)script, input, output, ctxt.uriMapper, newResultSetBuilder(script));
                        onResultsChanged();
                        if (checkStatusPort((XProcScript)script, output))
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

        protected JobResultSet.Builder newResultSetBuilder(Script script) {
                return new JobResultSet.Builder(script);
        }

        // package private for unit tests
        static JobResultSet buildResultSet(XProcScript script, XProcInput inputs, XProcOutput outputs, URIMapper mapper)
                        throws IOException {
                return buildResultSet(script, inputs, outputs, mapper, new JobResultSet.Builder(script));
        }

        private static JobResultSet buildResultSet(XProcScript script, XProcInput inputs, XProcOutput outputs,
                                                   URIMapper mapper, JobResultSet.Builder builder) throws IOException {

                // iterate over output ports
                for (ScriptPort port : script.getOutputPorts()) {
                        String mediaType = port.getMediaType();

                        // check if it is implemented as an output option
                        XProcScriptOption option = script.getResultOption(port.getName());
                        if (option != null) {
                                if (inputs.getOptions().get(option.getXProcOptionName()) == null)
                                        // option was not set
                                        continue;
                                if (XProcOptionMetadata.ANY_FILE_URI.equals(option.getType().getId())) {
                                        URI path; {
                                                Object val = inputs.getOptions().get(option.getXProcOptionName());
                                                try {
                                                        path = URI.create((String)val);
                                                } catch (ClassCastException e) {
                                                        throw new RuntimeException(
                                                                "Expected string value for option " + option.getName()
                                                                + " but got: " + val.getClass());
                                                }
                                        }
                                        File f = new File(path);
                                        if (f.exists()) {
                                                builder = builder.addResult(port.getName(),
                                                                            mapper.unmapOutput(path).toString(),
                                                                            f,
                                                                            mediaType);
                                        }
                                } else if (XProcOptionMetadata.ANY_DIR_URI.equals(option.getType().getId())) {
                                        String dir; {
                                                Object val = inputs.getOptions().get(option.getXProcOptionName());
                                                try {
                                                        dir = (String)val;
                                                } catch (ClassCastException e) {
                                                        throw new RuntimeException(
                                                                "Expected string value for option " + option.getName()
                                                                + " but got: " + val.getClass());
                                                }
                                        }
                                        // scan the directory to get all files inside and write them to the XProcOutput
                                        for (File f : IOHelper.treeFileList(new File(URI.create(dir)))) {
                                                URI path = f.toURI();
                                                builder = builder.addResult(port.getName(),
                                                                            mapper.unmapOutput(path).toString(),
                                                                            f,
                                                                            mediaType);
                                        }
                                }
                        } else {
                                Supplier<Result> resultProvider = outputs.getResultProvider(port.getName());
                                if (resultProvider == null)
                                        // XProcDecorator makes sure this can not happen
                                        continue;
                                if (!(resultProvider instanceof DynamicResultProvider))
                                        // XProcDecorator makes sure this can not happen
                                        throw new RuntimeException(
                                                "Result supplier is expected to be a DynamicResultProvider but got: " + resultProvider);
                                for (Result result : ((DynamicResultProvider)resultProvider).providedResults()) {
                                        String sysId = result.getSystemId();
                                        if (sysId == null)
                                                // XProcDecorator makes sure this can not happen
                                                throw new RuntimeException(
                                                        "Result is expected to be a DynamicResult but got: " + result);
                                        URI path = URI.create(sysId);
                                        builder = builder.addResult(port.getName(),
                                                                    mapper.unmapOutput(path).toString(),
                                                                    new File(path),
                                                                    mediaType);
                                }
                        }
                }
                return builder.build();
        }

        /**
         * Check the validation status from the status port and get it's value.
         */
        // package private for unit tests
        static boolean checkStatusPort(XProcScript script, XProcOutput outputs) {
                Optional<ScriptPort> statusPort = script.getStatusPort();
                if (statusPort.isPresent()) {
                        Supplier<Result> provider = outputs.getResultProvider(statusPort.get().getName());
                        if (provider != null && provider instanceof StatusResultProvider) { // should always be true
                                boolean ok = true;
                                for (InputStream status : ((StatusResultProvider)provider).read()) {
                                        ok &= processStatus(status);
                                }
                                return ok;
                        }
                }
                return true;
        }

        /**
         * Read the XML file to check that validation status is equal to "ok".
         */
        // package private for unit tests
        static boolean processStatus(InputStream status) {
                // check the contents of the xml and check if result is "ok"
                // <d:status xmlns:d="http://www.daisy.org/ns/pipeline/data" result="error"/>
                try {
                        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                        docBuilderFactory.setNamespaceAware(true);
                        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                        Document doc = docBuilder.parse(status);
                        String result = doc.getDocumentElement().getAttribute("result");
                        if (result == null || result.isEmpty()) {
                                throw new RuntimeException("No result attribute was found in the status port");
                        }
                        return result.equalsIgnoreCase("ok");

                } catch (Exception e) {
                        throw new RuntimeException("Error processing status file", e);
                }
        }
}
