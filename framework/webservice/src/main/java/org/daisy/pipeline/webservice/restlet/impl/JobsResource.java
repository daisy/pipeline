package org.daisy.pipeline.webservice.restlet.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.ZippedJobResources;
import org.daisy.pipeline.script.BoundScript;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.webservice.impl.PosterCallback;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.request.JobRequest;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.restlet.MultipartRequestData;
import org.daisy.pipeline.webservice.xml.JobXmlWriter;
import org.daisy.pipeline.webservice.xml.JobsXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

public class JobsResource extends AuthenticatedResource {

        private static final String JOB_DATA_FIELD = "job-data";
        private static final String JOB_REQUEST_FIELD = "job-request";

        /** The logger. */
        private static Logger logger = LoggerFactory.getLogger(JobsResource.class.getName());

        /**
         * Gets the resource.
         *
         * @return the resource
         */
        @Get("xml")
        public Representation getResource() {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }
                JobManager jobMan = getJobManager(this.getClient());
                JobsXmlWriter writer = new JobsXmlWriter(
                        jobMan.getJobs(),
                        getJobManager(getStorage().getClientStorage().defaultClient()).getExecutionQueue(),
                        getRequest().getRootRef().toString(),
                        getWebSocketRootRef().toString());
                if (getConfiguration().isLocalFS()){
                	writer.withLocalPaths();
                }
                Document doc = writer.getXmlDocument();
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, doc);
                setStatus(Status.SUCCESS_OK);
                logResponse(dom);
                return dom;
        }


        /**
         * Creates the resource.
         *
         * @param representation the representation
         * @return the representation
         * @throws Exception the exception
         */
        @Post
        public Representation createResource(Representation representation) {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }

                Document doc = null;
                ZipFile zipfile = null;
                if (representation == null)
                        ; // everything will be in the query string
                else if (MediaType.MULTIPART_FORM_DATA.equals(representation.getMediaType(), true)) {
                        Request request = getRequest();
                        // sort through the multipart request
                        MultipartRequestData data = null;
                        try {
                                data = MultipartRequestData.processMultipart(request,
                                                                             JOB_DATA_FIELD,
                                                                             JOB_REQUEST_FIELD,
                                                                             new File(getConfiguration().getTmpDir()));
                        } catch (Exception e) {
                                return badRequest(e);
                        }
                        if (data == null) {
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                                return this.getErrorRepresentation("Multipart data is empty");
                        }
                        doc = data.getXml(); // may be null
                        zipfile = data.getZipFile();
                } else if (MediaType.APPLICATION_ZIP.equals(representation.getMediaType(), true)) {
                        // data is in the ZIP, request is in the query string
                        // FIXME: I could not make this work: perhaps it is Restlet, or perhaps it
                        // is cURL, but the ZIP file is not identical to the uploaded file
                        logger.debug("Reading zip file");
                        try {
                                File tmp = File.createTempFile("p2ws", ".zip", new File(getConfiguration().getTmpDir()));
                                try (FileOutputStream fos = new FileOutputStream(tmp)) {
                                        representation.write(fos);
                                }
                                zipfile = new ZipFile(tmp);
                        } catch (Exception e) {
                                return badRequest(e);
                        }
                } else {
                        // assuming XML - all data should be inline or on local file system
                        String xml = null;
                        try {
                                xml = representation.getText();
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                factory.setNamespaceAware(true);
                                DocumentBuilder builder = factory.newDocumentBuilder();
                                InputSource is = new InputSource(new StringReader(xml));
                                doc = builder.parse(is);
                        } catch (IOException|ParserConfigurationException|SAXException e) {
                                if (xml != null && logger.isDebugEnabled())
                                        logger.debug("Request XML: " + xml);
                                return badRequest(e);
                        }
                }
                JobRequest req; {
                        if (doc != null) {
                                try {
                                        req = JobRequest.fromXML(doc);
                                } catch (IllegalArgumentException e) {
                                        if (doc != null && logger.isDebugEnabled())
                                                logger.debug("Request XML: " + XmlUtils.nodeToString(doc));
                                        return badRequest(e);
                                }
                        } else {
                                try {
                                        req = JobRequest.fromQuery(getQuery());
                                } catch (IllegalArgumentException e) {
                                        return badRequest(e);
                                }
                        }
                }
                Optional<Job> job; {
                        try {
                                job = createJob(req, zipfile);
                        } catch (LocalInputException|FileNotFoundException|IllegalArgumentException e) {
                                return badRequest(e);
                        }
                }
                if (!job.isPresent()) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return getErrorRepresentation("Could not create job ");
                }

                // store the config
                getStorage().getJobConfigurationStorage().add(
                        job.get().getId(),
                        doc != null ? XmlUtils.nodeToString(doc) : req.toJSON());

                // Note that we're not using JobXmlWriter's messagesThreshold argument. It is no use because
                // filtering of messages on log level already happens in MessageBus and JobProgressAppender.
                JobXmlWriter writer = new JobXmlWriter(job.get(),
                                                       getRequest().getRootRef().toString(),
                                                       getWebSocketRootRef().toString());
                if (job.get().getStatus() == Job.Status.IDLE) {
                        writer.withPriority(getJobPriority(job.get()));
                }
                Document jobXml = writer.withScriptDetails()
                                        .withNotificationsAttribute()
                                        .getXmlDocument();

                // initiate callbacks
                registerCallbacks(job.get(), req.getCallbacks());

                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, jobXml);
                setStatus(Status.SUCCESS_CREATED);
                logResponse(dom);
                return dom;

        }

        private Priority getJobPriority(Job job) {
                return getJobManager(getStorage().getClientStorage().defaultClient()).getExecutionQueue().getJobPriority(job.getId());
        }

        private Representation badRequest(Exception e) {
                logger.error("Bad request:", e);
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return getErrorRepresentation(e.getMessage());
        }

        /**
         * Creates the job.
         */
        private Optional<Job> createJob(JobRequest req, ZipFile zip) throws LocalInputException, FileNotFoundException {
                // get the script from the script ID
                ScriptService<?> scriptService = getScriptRegistry().getScript(req.getScriptId());
                if (scriptService == null) {
                        logger.error("Script not found");
                        return Optional.absent();
                }
                Script script = scriptService.load();
                JobResources resourceCollection = zip != null ? new ZippedJobResources(zip) : null;
                BoundScript.Builder bound = new BoundScript.Builder(script, resourceCollection);
                addInputsToJob(req.getInputs(), script, bound, zip != null);
                addOptionsToJob(req.getOptions(), script, bound, zip != null);

                if (req.isOutputElementUsed()) {
                        // show deprecation warning in server logs
                        logger.warn("Deprecated <output/> element used. Job results should be retrieved through the /jobs/ID/result API.");
                        // show deprecation warning in response header
                        addWarningHeader(
                                199,
                                "\"Deprecated API\": "
                                + "<output/> is deprecated, job results should be retrieved through the /jobs/ID/result API");
                }

                logger.debug(String.format("Job's nice name: %s", req.getNiceName()));
                logger.debug(String.format("Job's batch ID: %s", req.getBatchId()));
                logger.debug(String.format("Job's priority: %s", req.getPriority()));
                JobManager jobMan = getJobManager(getClient());
                return jobMan.newJob(bound.build())
                             .withNiceName(req.getNiceName())
                             .withBatchId(req.getBatchId())
                             .withPriority(req.getPriority())
                             .build();
        }

        /**
         * Initiate callbacks declared in the job request.
         */
        private void registerCallbacks(Job job, List<JobRequest.Callback> callbacks) {
                if (callbacks.size() > 0) {
                        // show deprecation warning in server logs
                        logger.warn("Deprecated <callback/> element used. Push notifications should be retrieved through websocket connection.");
                        // show deprecation warning in response header
                        addWarningHeader(
                                199,
                                "\"Deprecated API\": "
                                + "<callback/> is deprecated, push notifications should be retrieved through websocket connection");
                }
                for (JobRequest.Callback callback : callbacks) {
                        CallbackHandler handler = getCallbackHandler();
                        if (handler == null) {
                                throw new RuntimeException("No push notifier");
                        }
                        // Note that the frequency does not have any effect: frequency is hard-coded in
                        // PushNotifier. Because we already have the deprecation message, don't warn about this.
                        handler.addCallback(new PosterCallback(job,
                                                               callback.getType(),
                                                               callback.getFrequency(),
                                                               callback.getHref(),
                                                               getClient(),
                                                               getRequest().getRootRef().toString()));
                }
        }

        private void addInputsToJob(Map<String,List<SAXSource>> inputs, Script script, BoundScript.Builder builder, boolean zippedContext)
                        throws LocalInputException, FileNotFoundException {

                for (ScriptPort input : script.getInputPorts()) {
                        String name = input.getName();
                        if (inputs.containsKey(name)) {
                                for (SAXSource src : inputs.get(name)) {
                                        InputSource is = src.getInputSource();
                                        if (is.getCharacterStream() != null || is.getByteStream() != null)
                                                builder.withInput(name, src);
                                        else {
                                                URI uri = URI.create(src.getSystemId());
                                                checkInput(uri, zippedContext);
                                                builder.withInput(name, uri);
                                        }
                                }
                        }
                }
        }
        
        private void addOptionsToJob(Map<String,List<String>> options, Script script, BoundScript.Builder builder, boolean zippedContext)
                        throws LocalInputException, FileNotFoundException {

                for (ScriptOption option : script.getOptions()) {
                        String name = option.getName();
                        if (options.containsKey(name)) {
                                boolean isInput = "anyDirURI".equals(option.getType().getId())
                                        || "anyFileURI".equals(option.getType().getId());
                                for (String val : options.get(name)) {
                                        if (isInput) {
                                                checkInput(URI.create(val), zippedContext);
                                        }
                                        builder.withOption(name, val);
                                }
                        }
                }
        }

        private void checkInput(URI uri, boolean zipFileSupplied) throws LocalInputException {
                if ("file".equals(uri.getScheme())) {
                        if (!getConfiguration().isLocalFS()) {
                                throw new LocalInputException(
                                        "WS does not allow local inputs but a href starting with 'file:' was found " + uri);
                        } else if (zipFileSupplied) {
                                throw new LocalInputException("You can't supply the data uri " + uri);
                        }
                }
        }

        private class LocalInputException extends Exception{
                private static final long serialVersionUID = 1L;

                public LocalInputException(String message){
                        super(message);
                }
        }
}
