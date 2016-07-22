package org.daisy.pipeline.webservice.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.daisy.common.priority.Priority;
import org.daisy.common.transform.LazySaxResultProvider;
import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.ZippedJobResources;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;
import org.daisy.pipeline.webserviceutils.callback.Callback;
import org.daisy.pipeline.webserviceutils.callback.Callback.CallbackType;
import org.daisy.pipeline.webserviceutils.xml.JobXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.JobsXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlUtils;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
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
import com.google.common.base.Supplier;

// TODO: Auto-generated Javadoc
/**
 * The Class JobsResource.
 */
public class JobsResource extends AuthenticatedResource {

        /** The tempfile prefix. */
        private final String tempfilePrefix = "p2ws";
        private final String tempfileSuffix = ".zip";

        private final String JOB_DATA_FIELD = "job-data";
        private final String JOB_REQUEST_FIELD = "job-request";

        /** The logger. */
        private static Logger logger = LoggerFactory.getLogger(JobsResource.class.getName());

        /**
         * Gets the resource.
         *
         * @return the resource
         */
        @Get("xml")
        public Representation getResource() {
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }
                JobManager jobMan = webservice().getJobManager(this.getClient());
                JobsXmlWriter writer = XmlWriterFactory.createXmlWriterForJobs(jobMan.getJobs());
                if(this.webservice().getConfiguration().isLocalFS()){
                	writer.withLocalPaths();
                }
                Document doc = writer.getXmlDocument();
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, doc);
                setStatus(Status.SUCCESS_OK);
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
                        try{
                                data = processMultipart(request);
                        }catch(Exception e){
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                                return this.getErrorRepresentation(e);
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
                                logger.error(e.getMessage());
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                                return this.getErrorRepresentation(e);
                        } catch (ParserConfigurationException e) {
                                logger.error(e.getMessage());
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                                return this.getErrorRepresentation(e);
                        } catch (SAXException e) {
                                logger.error(e.getMessage());
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                                return this.getErrorRepresentation(e);
                        }
                }

                ValidationStatus status= Validator.validateJobRequest(doc, webservice());

                if (!status.isValid()) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return this.getErrorRepresentation(status.getMessage());
                }

                Optional<Job> job;
                try {
                        job = createJob(doc, zipfile );
                } catch (LocalInputException e) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return this.getErrorRepresentation(e.getMessage());
                } catch (IllegalArgumentException iea) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return this.getErrorRepresentation(iea.getMessage());
                }

                if (!job.isPresent()) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return this.getErrorRepresentation("Could not create job ");
                }
                
                //store the config
                webservice().getStorage().getJobConfigurationStorage()
                        .add(job.get().getId(),XmlUtils.DOMToString(doc));

                JobXmlWriter writer = XmlWriterFactory.createXmlWriterForJob(job.get());
                Document jobXml = writer.withAllMessages().withScriptDetails().getXmlDocument();
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, jobXml);
                setStatus(Status.SUCCESS_CREATED);
                return dom;

        }

        /*
         * taken from an example at:
         * http://wiki.restlet.org/docs_2.0/13-restlet/28-restlet/64-restlet.html
         */
        /**
         * Process multipart.
         *
         * @param request the request
         * @return the multipart request data
         */
        private MultipartRequestData processMultipart(Request request) throws Exception {

                String tmpdir = webservice().getConfiguration().getTmpDir();
                logger.debug("Tmpdir: "+tmpdir);
                // 1/ Create a factory for disk-based file items
                DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                fileItemFactory.setSizeThreshold(1000240);

                // 2/ Create a new file upload handler based on the Restlet
                // FileUpload extension that will parse Restlet requests and
                // generates FileItems.
                RestletFileUpload upload = new RestletFileUpload(fileItemFactory);
                List<FileItem> items;

                ZipFile zip = null;
                String xml = "";
                        items = upload.parseRequest(request);
                        Iterator<FileItem> it = items.iterator();
                        while (it.hasNext()) {
                                FileItem fi = it.next();
                                if (fi.getFieldName().equals(JOB_DATA_FIELD)) {
                                        logger.debug("Reading zip file");
                                        File file = File.createTempFile(tempfilePrefix, tempfileSuffix, new File(tmpdir));
                                        fi.write(file);

                                        // re-opening the file after writing to it
                                        File file2 = new File(file.getAbsolutePath());
                                        zip = new ZipFile(file2);
                                }
                                else if (fi.getFieldName().equals(JOB_REQUEST_FIELD)) {
                                        xml = fi.getString("utf-8");
                                        logger.debug("XML multi:"+xml);
                                }
                        }

                        if (zip == null || xml.length() == 0) {
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                                return null;
                        }

                        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                        docFactory.setNamespaceAware(true);
                        DocumentBuilder builder = docFactory.newDocumentBuilder();
                        InputSource is = new InputSource(new StringReader(xml));
                        Document doc = builder.parse(is);
                        MultipartRequestData data = new MultipartRequestData(zip, doc);
                        return data;

        }

        // just a convenience class for representing the parts of a multipart request
        /**
         * The Class MultipartRequestData.
         */
        private class MultipartRequestData {

                /**
                 * Process multipart.
                 *
                 * @param request the request
                 * @return the multipart request data
                 */
                /** The zip. */
                private final ZipFile zip;

                /** The xml. */
                private final Document xml;

                /**
                 * Instantiates a new multipart request data.
                 *
                 * @param zip the zip
                 * @param xml the xml
                 */
                MultipartRequestData(ZipFile zip, Document xml) {
                        this.zip = zip;
                        this.xml = xml;
                }

                /**
                 * Gets the zip file.
                 *
                 * @return the zip file
                 */
                ZipFile getZipFile() {
                        return zip;
                }

                /**
                 * Gets the xml.
                 *
                 * @return the xml
                 */
                Document getXml() {
                        return xml;
                }
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
                        throws LocalInputException {

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
                ScriptRegistry scriptRegistry = webservice().getScriptRegistry();
                XProcScriptService unfilteredScript = scriptRegistry.getScript(scriptId);
                if (unfilteredScript == null) {
                        logger.error("Script not found");
                        return Optional.absent();
                }
                XProcScript script = unfilteredScript.load();
                XProcInput.Builder inBuilder = new XProcInput.Builder();
                XProcOutput.Builder outBuilder = new XProcOutput.Builder();

                addInputsToJob(doc.getElementsByTagNameNS(Validator.NS_DAISY,"input"), script.getXProcPipelineInfo().getInputPorts(), inBuilder,zip!=null);

                /*Iterable<XProcOptionInfo> filteredOptions = null;
                  if (!((PipelineWebService) getApplication()).isLocal()) {
                  filteredOptions = XProcScriptFilter.INSTANCE.filter(script).getXProcPipelineInfo().getOptions();
                  }*/

                addOptionsToJob(doc.getElementsByTagNameNS(Validator.NS_DAISY,"option"), script, inBuilder,zip==null);// script.getXProcPipelineInfo().getOptions(), builder, filteredOptions);
                addOutputsToJob(doc.getElementsByTagNameNS(Validator.NS_DAISY,"output"), script.getXProcPipelineInfo().getOutputPorts(), outBuilder);

                BoundXProcScript bound= BoundXProcScript.from(script,inBuilder.build(),outBuilder.build());

                JobManager jobMan = webservice().getJobManager(this.getClient());
                JobResources resourceCollection=null;
                if (zip != null){
                        resourceCollection = new ZippedJobResources(zip);
                }
                boolean mapping=!webservice().getConfiguration().isLocalFS();
                //logger.debug("MAPPING "+mapping);
                Optional<Job> newJob= jobMan.newJob(bound).isMapping(mapping)
                        .withNiceName(niceName).withBatchId(JobIdFactory.newBatchIdFromString(batchId))
                        .withPriority(priority).withResources(resourceCollection).build();
                if(!newJob.isPresent()){
                        return Optional.absent();
                }

                NodeList callbacks = doc.getElementsByTagNameNS(Validator.NS_DAISY,"callback");
                for (int i = 0; i<callbacks.getLength(); i++) {
                        Element elm = (Element)callbacks.item(i);
                        String href = elm.getAttribute("href");
                        CallbackType type = CallbackType.valueOf(elm.getAttribute("type").toUpperCase());
                        String frequency = elm.getAttribute("frequency");
                        Callback callback = null;
                        int freq = 0;
                        if (frequency.length() > 0) {
                                freq = Integer.parseInt(frequency);
                        }

                        try {
                                callback = new Callback(newJob.get().getId(), this.getClient(), new URI(href), type, freq);
                        } catch (URISyntaxException e) {
                                logger.warn("Cannot create callback: " + e.getMessage());
                        }

                        if (callback != null) {
                                webservice().getCallbackRegistry().addCallback(callback);
                        }
                }
                return newJob;
        }

        /**
         * Adds the inputs to job.
         *
         * @param nodes the nodes
         * @param inputPorts the input ports
         * @param builder the builder
         * @throws LocalInputException
         */
        private void addInputsToJob(NodeList nodes,
                        Iterable<XProcPortInfo> inputPorts, XProcInput.Builder builder,boolean zippedContext)
                        throws LocalInputException {

                Iterator<XProcPortInfo> it = inputPorts.iterator();
                while (it.hasNext()) {
                        XProcPortInfo input = it.next();
                        String inputName = input.getName();
                        for (int i = 0; i < nodes.getLength(); i++) {
                                Element inputElm = (Element) nodes.item(i);
                                String name = inputElm.getAttribute("name");
                                if (name.equals(inputName)) {
                                        NodeList fileNodes = inputElm.getElementsByTagNameNS(Validator.NS_DAISY,"item");
                                        NodeList docwrapperNodes = inputElm.getElementsByTagNameNS(Validator.NS_DAISY,"docwrapper");

                                        if (fileNodes.getLength() > 0) {
                                                for (int j = 0; j < fileNodes.getLength(); j++) {
                                                        String src = ((Element)fileNodes.item(j)).getAttribute("value");
                                                        this.checkInput(src,zippedContext);
                                                        LazySaxSourceProvider prov= new LazySaxSourceProvider(src);
                                                        builder.withInput(name, prov);
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

                                                        final SAXSource source = new SAXSource();

                                                        // TODO any way to get Source directly from a node?
                                                        //
                                                        String xml = XmlUtils.nodeToString(content);
                                                        InputSource is = new org.xml.sax.InputSource(new java.io.StringReader(xml));
                                                        source.setInputSource(is);
                                                        Supplier<Source> prov= new Supplier<Source>(){
                                                                @Override
                                                                public Source get(){
                                                                        return source;
                                                                }
                                                        };
                                                        builder.withInput(name, prov);
                                                }
                                        }
                                }
                        }
                }

        }

        /**
         * Adds the inputs to job.
         *
         * @param nodes the nodes
         * @param inputPorts the input ports
         * @param builder the builder
         */
        private void addOutputsToJob(NodeList nodes, Iterable<XProcPortInfo> ports, XProcOutput.Builder builder) {
                if(!webservice().getConfiguration().isLocalFS()){
                        return;
                }

                for(XProcPortInfo output : ports){
                        String outputName = output.getName();
                        for (int i = 0; i < nodes.getLength(); i++) {
                                Element outputElm = (Element) nodes.item(i);
                                String name = outputElm.getAttribute("name");
                                if (name.equals(outputName)) {
                                        NodeList fileNodes = outputElm.getElementsByTagNameNS(Validator.NS_DAISY,"item");

                                        for (int j = 0; j < fileNodes.getLength(); j++) {
                                                String res = ((Element)fileNodes.item(j)).getAttribute("value");
                                                LazySaxResultProvider prov= new LazySaxResultProvider(res);
                                                builder.withOutput(name, prov);
                                        }
                                }
                        }
                }

        }
        
        /**
         * Adds the options to job.
         * @throws LocalInputException
         */
        //private void addOptionsToJob(NodeList nodes, Iterable<XProcOptionInfo> allOptions, XProcInput.Builder builder, Iterable<XProcOptionInfo> filteredOptions) {
        private void addOptionsToJob(NodeList nodes, XProcScript script,
                        XProcInput.Builder builder,boolean zippedContext) throws LocalInputException {

                Iterable<XProcOptionInfo> allOptions = script.getXProcPipelineInfo().getOptions();

                Iterable<XProcOptionInfo> filteredOptions = null;
                filteredOptions = XProcScriptFilter.INSTANCE.filter(script).getXProcPipelineInfo().getOptions();

                Iterator<XProcOptionInfo> it = allOptions.iterator();
                while(it.hasNext()) {
                        XProcOptionInfo opt = it.next();
                        String optionName = opt.getName().toString();
                        // if we are filtering options, then check to ensure that this particular option exists in the filtered set
                        if (filteredOptions != null) {
                                Iterator<XProcOptionInfo> itFilter = filteredOptions.iterator();
                                boolean found = false;
                                while (itFilter.hasNext()) {
                                        String filteredOptName = itFilter.next().getName().toString();
                                        if (filteredOptName.equals(optionName)) {
                                                found = true;
                                                break;
                                        }
                                }

                                // if the option did not exist in the filtered set of options
                                // then we are not allowed to set it
                                // however, it still requires a value, so set it to ""
                                if (!found) {
                                        builder.withOption(new QName(optionName), "");
                                        continue;
                                }
                        }

                        // this is an option we are allowed to set. so, look for the option in the job request doc.
                        for (int i = 0; i< nodes.getLength(); i++) {
                                Element optionElm = (Element) nodes.item(i);
                                String name = optionElm.getAttribute("name");
                                XProcOptionMetadata metadata = script.getOptionMetadata(new QName(name));
                                if (metadata==null){
                                        throw new IllegalArgumentException(String.format("Option %s is not recognized by script %s",name,script.getName()));
                                }

                                //if input we have to check
                                if (name.equals(optionName)) {
                                        boolean isInput = metadata.getType()== "anyDirURI" ||metadata.getType()== "anyFileURI";
                                        //eventhough the option is a sequence it may happen that 
                                        //there are no item elements, just one value
                                        NodeList items = optionElm.getElementsByTagNameNS(Validator.NS_DAISY,"item");
                                        if (metadata.isSequence() && items.getLength()>0) {
                                                // concat items
                                                String val = ((Element)items.item(0)).getAttribute("value");

                                                for (int j = 1; j<items.getLength(); j++) {
                                                        Element e = (Element)items.item(j);
                                                        if(isInput){
                                                                checkInput(e.getAttribute("value"),zippedContext);
                                                        }
                                                        val += metadata.getSeparator() + e.getAttribute("value");
                                                }
                                                builder.withOption(new QName(name), val);
                                        }
                                        else {
                                                String val = optionElm.getTextContent();
                                                if(isInput){
                                                        checkInput(val,zippedContext);
                                                }
                                                builder.withOption(new QName(name), val);
                                                break;
                                        }

                                }
                        }
                }
        }
        private void checkInput(String uri,boolean zipFileSupplied) throws LocalInputException{ 
                //if the uri file starts with "file" but we're not executing in
                //localfs mode send exception
                if (uri.contains("file:") && ! this.webservice().getConfiguration().isLocalFS()){
                        throw new LocalInputException("WS does not allow local inputs but a href starting with 'file:' was found "+uri);
                }
                if (uri.contains("file:") && zipFileSupplied){
                        throw new LocalInputException("You can't supply the data uri "+uri);
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
