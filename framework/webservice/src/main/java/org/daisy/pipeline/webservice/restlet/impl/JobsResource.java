package org.daisy.pipeline.webservice.restlet.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.ZippedJobResources;
import org.daisy.pipeline.script.BoundScript;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.webservice.Callback.CallbackType;
import org.daisy.pipeline.webservice.impl.PosterCallback;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.restlet.MultipartRequestData;
import org.daisy.pipeline.webservice.xml.JobXmlWriter;
import org.daisy.pipeline.webservice.xml.JobsXmlWriter;
import org.daisy.pipeline.webservice.xml.ValidationStatus;
import org.daisy.pipeline.webservice.xml.Validator;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

// TODO: Auto-generated Javadoc
/**
 * The Class JobsResource.
 */
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

                if (representation == null) {
                        // POST request with no entity.
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return this.getErrorRepresentation("POST request with no entity");
                }


                Document doc = null;
                ZipFile zipfile = null;

                if (MediaType.MULTIPART_FORM_DATA.equals(representation.getMediaType(), true)) {
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
                        doc = data.getXml();
                        zipfile = data.getZipFile();
                }
                // else it's not multipart; all data should be inline.
                else {
                        String s;
                        try {
                                s = representation.getText();
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                factory.setNamespaceAware(true);
                                DocumentBuilder builder = factory.newDocumentBuilder();
                                InputSource is = new InputSource(new StringReader(s));
                                doc = builder.parse(is);
                        } catch (IOException e) {
                                return badRequest(e);
                        } catch (ParserConfigurationException e) {
                                return badRequest(e);
                        } catch (SAXException e) {
                                return badRequest(e);
                        }
                }
                if (logger.isDebugEnabled())
                        logger.debug(XmlUtils.nodeToString(doc));

                ValidationStatus status = Validator.validateJobRequest(doc, getScriptRegistry());

                if (!status.isValid()) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return this.getErrorRepresentation(status.getMessage());
                }

                Optional<Job> job;
                try {
                        job = createJob(doc, zipfile );
                } catch (LocalInputException e) {
                        return badRequest(e);
                } catch (FileNotFoundException e) {
                        return badRequest(e);
                } catch (IllegalArgumentException e) {
                        return badRequest(e);
                }

                if (!job.isPresent()) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return this.getErrorRepresentation("Could not create job ");
                }
                
                //store the config
                getStorage().getJobConfigurationStorage()
                    .add(job.get().getId(), XmlUtils.nodeToString(doc));

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
                registerCallbacks(job.get(), doc);

                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, jobXml);
                setStatus(Status.SUCCESS_CREATED);
                logResponse(dom);
                return dom;

        }

        private Priority getJobPriority(Job job) {
                return getJobManager(getStorage().getClientStorage().defaultClient()).getExecutionQueue().getJobPriority(job.getId());
        }

        private Representation badRequest(Exception e) {
                logger.error("bad request:", e);
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return this.getErrorRepresentation(e.getMessage());
        }

        /**
         * Creates the job.
         *
         * @param doc the doc
         * @param zip the zip
         * @return the job
         * @throws LocalInputException
         */
        private Optional<Job> createJob(Document doc, ZipFile zip)
                        throws LocalInputException, FileNotFoundException {

                Element scriptElm = (Element) doc.getElementsByTagNameNS(Validator.NS_DAISY, "script").item(0);
                Priority priority=Priority.MEDIUM;
                String niceName="";
                //get nice name
                NodeList elems=doc.getElementsByTagNameNS(Validator.NS_DAISY,"nicename"); 
                if(elems.getLength()!=0)
                        niceName=elems.item(0).getTextContent();
                logger.debug(String.format("Job's nice name: %s",niceName));
                String batchId="";
                //get the batch name 
                elems=doc.getElementsByTagName("batchId"); 
                if(elems.getLength()!=0)
                        batchId=elems.item(0).getTextContent();
                logger.debug(String.format("Job's batch id: %s",batchId));

                //get priority
                elems=doc.getElementsByTagNameNS(Validator.NS_DAISY,"priority"); 
                if(elems.getLength()!=0){
                        String prioString=elems.item(0).getTextContent();
                        priority=Priority.valueOf(prioString.toUpperCase());
                }
                
                logger.debug(String.format("Jobs priority: %s",priority));


                // TODO eventually we might want to have an href-script ID lookup table
                // but for now, we'll get the script ID from the last part of the URL
                String scriptId = scriptElm.getAttribute("href");
                if (scriptId.endsWith("/")) {
                        scriptId = scriptId.substring(0, scriptId.length() - 1);
                }
                int idx = scriptId.lastIndexOf('/');
                scriptId = scriptId.substring(idx+1);

                // get the script from the ID
                ScriptService<?> scriptService = getScriptRegistry().getScript(scriptId);
                if (scriptService == null) {
                        logger.error("Script not found");
                        return Optional.absent();
                }
                Script script = scriptService.load();
                JobResources resourceCollection = zip != null ? new ZippedJobResources(zip) : null;
                BoundScript.Builder bound = new BoundScript.Builder(script, resourceCollection);

                addInputsToJob(doc.getElementsByTagNameNS(Validator.NS_DAISY,"input"), script, bound, zip != null);
                addOptionsToJob(doc.getElementsByTagNameNS(Validator.NS_DAISY,"option"), script, bound, zip != null);
                if (doc.getElementsByTagNameNS(Validator.NS_DAISY,"output").getLength() > 0) {
                        // show deprecation warning in server logs
                        logger.warn("Deprecated <output/> element used. Job results should be retrieved through the /jobs/ID/result API.");
                        // show deprecation warning in response header
                        addWarningHeader(
                                199,
                                "\"Deprecated API\": "
                                + "<output/> is deprecated, job results should be retrieved through the /jobs/ID/result API");
                }

                JobManager jobMan = getJobManager(this.getClient());
                return jobMan.newJob(bound.build())
                        .withNiceName(niceName).withBatchId(JobIdFactory.newBatchIdFromString(batchId))
                        .withPriority(priority).build();
        }

        /**
         * Initiate callbacks declared in the job request XML.
         *
         * @param doc The job request XML.
         */
        private void registerCallbacks(Job job, Document doc) {
                NodeList callbacks = doc.getElementsByTagNameNS(Validator.NS_DAISY,"callback");
                if (callbacks.getLength() > 0) {
                        // show deprecation warning in server logs
                        logger.warn("Deprecated <callback/> element used. Push notifications should be retrieved through websocket connection.");
                        // show deprecation warning in response header
                        addWarningHeader(
                                199,
                                "\"Deprecated API\": "
                                + "<callback/> is deprecated, push notifications should be retrieved through websocket connection");
                }
                for (int i = 0; i<callbacks.getLength(); i++) {
                        Element elm = (Element)callbacks.item(i);
                        CallbackType type = CallbackType.valueOf(elm.getAttribute("type").toUpperCase());
                        // Note that this attribute does not have any effect: frequency is hard-coded in
                        // PushNotifier. Because we already have the deprecation message, don't warn
                        // about this.
                        String frequency = elm.getAttribute("frequency");
                        int freq = 0;
                        if (frequency.length() > 0) {
                                freq = Integer.parseInt(frequency);
                        }
                        try {
                                URI href = new URI(elm.getAttribute("href"));
                                CallbackHandler handler = getCallbackHandler();
                                if (handler == null) {
                                        throw new RuntimeException("No push notifier");
                                }
                                handler.addCallback(new PosterCallback(job, type, freq, href, getClient(),
                                                                       getRequest().getRootRef().toString()));
                        } catch (URISyntaxException e) {
                                logger.warn("Cannot create callback: " + e.getMessage());
                        }
                }
        }

        /**
         * Adds the inputs to job.
         *
         * @param nodes the nodes
         * @param inputPorts the input ports
         * @param builder the builder
         * @throws LocalInputException
         */
        private void addInputsToJob(NodeList nodes, Script script, BoundScript.Builder builder, boolean zippedContext)
                        throws LocalInputException, FileNotFoundException {

                for (ScriptPort input : script.getInputPorts()) {
                        String inputName = input.getName();
                        for (int i = 0; i < nodes.getLength(); i++) {
                                Element inputElm = (Element) nodes.item(i);
                                String name = inputElm.getAttribute("name");
                                if (name.equals(inputName)) {
                                        NodeList fileNodes = inputElm.getElementsByTagNameNS(Validator.NS_DAISY,"item");
                                        NodeList docwrapperNodes = inputElm.getElementsByTagNameNS(Validator.NS_DAISY,"docwrapper");

                                        if (fileNodes.getLength() > 0) {
                                                for (int j = 0; j < fileNodes.getLength(); j++) {
                                                        URI src = URI.create(((Element)fileNodes.item(j)).getAttribute("value"));
                                                        checkInput(src, zippedContext);
                                                        builder.withInput(name, src);
                                                }
                                        }
                                        else {
                                                for (int j = 0; j< docwrapperNodes.getLength(); j++){
                                                        Element docwrapper = (Element)docwrapperNodes.item(j);
                                                        Node content = null;
                                                        // find the first element child
                                                        for (int q = 0; q < docwrapper.getChildNodes().getLength(); q++) {
                                                                if (docwrapper.getChildNodes().item(q).getNodeType() == Node.ELEMENT_NODE) {
                                                                        content = docwrapper.getChildNodes().item(q);
                                                                        break;
                                                                }
                                                        }
                                                        SAXSource source = new SAXSource();
                                                        String xml = XmlUtils.nodeToString(content);
                                                        InputSource is = new InputSource(new StringReader(xml));
                                                        source.setInputSource(is);
                                                        builder.withInput(name, source);
                                                }
                                        }
                                }
                        }
                }

        }
        
        /**
         * Adds the options to job.
         * @throws LocalInputException
         */
        private void addOptionsToJob(NodeList nodes, Script script, BoundScript.Builder builder, boolean zippedContext)
                        throws LocalInputException, FileNotFoundException {

                Iterable<ScriptOption> options = script.getOptions();
                for (ScriptOption option : options) {
                        for (int i = 0; i< nodes.getLength(); i++) {
                                Element optionElm = (Element) nodes.item(i);
                                String name = optionElm.getAttribute("name");
                                if (name.equals(option.getName())) {
                                        boolean isInput = "anyDirURI".equals(option.getType().getId())
                                                       || "anyFileURI".equals(option.getType().getId());
                                        //eventhough the option is a sequence it may happen that 
                                        //there are no item elements, just one value
                                        NodeList items = optionElm.getElementsByTagNameNS(Validator.NS_DAISY,"item");
                                        if (items.getLength() > 0) {
                                                // accept <item> children even if it is not a sequence option
                                                // but at most one (this is verified in BoundScript.Builder)
                                                for (int j = 0; j<items.getLength(); j++) {
                                                        Element e = (Element)items.item(j);
                                                        String v = e.getAttribute("value");
                                                        if(isInput){
                                                                checkInput(URI.create(v), zippedContext);
                                                        }
                                                        builder.withOption(option.getName(), v);
                                                }
                                        } else {
                                                // accept text node even if it is a sequence option
                                                String v = optionElm.getTextContent();
                                                if(isInput){
                                                        checkInput(URI.create(v), zippedContext);
                                                }
                                                builder.withOption(option.getName(), v);
                                        }
                                        break;
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
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                public LocalInputException(String message){
                        super(message);
                }
        }
}
