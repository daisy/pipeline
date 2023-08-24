package org.daisy.pipeline.webservice.xml;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashSet;
import java.util.List;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.ProgressMessage;
import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobXmlWriter {
        
        private final String baseUrl;
        private Job job = null;
        private List<Message> messages = null;
        private long messagesNewerThan = -1;
        private BigDecimal progress = null;
        private boolean scriptDetails = false;
        private boolean fullResult=false;
        private boolean localPaths=false; 
        private boolean onlyPrimaries=false; 

        private Job.Status statusOverWrite = null;
        private Priority priority = null;
        private int queuePosition = -1;
        private static Logger logger = LoggerFactory.getLogger(JobXmlWriter.class
                        .getName());

        private final HashSet<Level> MSG_LEVELS;

        /**
         * @param baseUrl Prefix to be included at the beginning of <code>href</code>
         *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
         *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
         *                absolute paths relative to the domain name.
         */
        public JobXmlWriter(Job job, String baseUrl) {
                this(job, Level.TRACE, baseUrl);
        }

        public JobXmlWriter(Job job, Level messagesThreshold, String baseUrl) {
                this.job = job;
                this.baseUrl = baseUrl;
                MSG_LEVELS = new HashSet<Level>();
                MSG_LEVELS.add(Level.ERROR);
                MSG_LEVELS.add(Level.WARNING);
                MSG_LEVELS.add(Level.INFO);
                if (Level.INFO.isMoreSevereThan(messagesThreshold))
                        MSG_LEVELS.add(Level.DEBUG);
        }

        public Document getXmlDocument() {
                if (job == null) {
                        logger.warn("Could not create XML for null job.");
                        return null;
                }
                return jobToXmlDocument();
        }

        // instead of creating a standalone XML document, add an element to an existing document
        public void addAsElementChild(Element parent) {
                Document doc = parent.getOwnerDocument();
                Element jobElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "job");
                addElementData(job, jobElm);
                parent.appendChild(jobElm);
        }

        public JobXmlWriter withScriptDetails() {
                scriptDetails = true;
                return this;
        }

        public JobXmlWriter withLocalPaths() {
                localPaths= true;
                return this;
        }

        public JobXmlWriter withAllMessages() {
                MessageAccessor accessor = job.getMonitor().getMessageAccessor();
                if (accessor != null) {
                        progress = accessor.getProgress();
                        messages = accessor.createFilter().filterLevels(MSG_LEVELS).getMessages();
                }
                return this;
        }

        public JobXmlWriter withNewMessages(int newerThan) {
                MessageAccessor accessor = job.getMonitor().getMessageAccessor();
                if (accessor != null) {
                        withProgress(accessor.getProgress());
                        withMessages(accessor.createFilter().filterLevels(MSG_LEVELS)
                                             .greaterThan(newerThan).getMessages(),
                                     newerThan);
                }
                return this;
        }

        public JobXmlWriter withMessages(List<Message> messages, int newerThan) {
                this.messages = messages;
                messagesNewerThan = newerThan;
                return this;
        }

        public JobXmlWriter withProgress(BigDecimal progress) {
                this.progress = progress;
                return this;
        }

        public void withFullResults(boolean fullResult) {
                this.fullResult =fullResult;
        }
        
        public void withOnlyPrimaries(boolean primaries) {
                this.onlyPrimaries=primaries;
        }
        public void overwriteStatus(Job.Status status) {
                this.statusOverWrite=status;
        }
        private Document jobToXmlDocument() {
                Document doc = XmlUtils.createDom("job");
                Element jobElm = doc.getDocumentElement();
                addElementData(job, jobElm);
                
                // for debugging only
                if (!XmlValidator.validate(doc, XmlValidator.JOB_SCHEMA_URL)) {
                        logger.error("INVALID XML:\n" + XmlUtils.nodeToString(doc));
                }

                return doc;
        }
        
        private void addElementData(Job job, Element element) {
                Document doc = element.getOwnerDocument();
                Job.Status status = (this.statusOverWrite==null)?job.getStatus():this.statusOverWrite;
                String jobHref = baseUrl + Routes.JOB_ROUTE.replaceFirst("\\{id\\}", job.getId().toString());
                
                element.setAttribute("id", job.getId().toString());
                element.setAttribute("href", jobHref);
                element.setAttribute("status", status.toString());
                if (priority != null) {
                        element.setAttribute("priority", priority.toString().toLowerCase());
                }
                if (queuePosition != -1) {
                        element.setAttribute("queue-position",String.format("%d", queuePosition));
                }
                if (!job.getNiceName().isEmpty()) {
                        Element nicenameElem= doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "nicename");
                        nicenameElem.setTextContent(job.getNiceName());
                        element.appendChild(nicenameElem);
                }
                if (!job.getBatchId().toString().isEmpty()) {
                        Element batchId= doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "batchId");
                        batchId.setTextContent(job.getBatchId().toString());
                        element.appendChild(batchId);
                }

                if (scriptDetails) {
                        Script script = job.getScript();
                        ScriptXmlWriter writer = new ScriptXmlWriter(script, baseUrl);
                        writer.addAsElementChild(element);
                }
                
                if (progress != null && status == Job.Status.RUNNING || messages != null && messages.size() > 0) {
                        Element messagesElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "messages");
                        element.appendChild(messagesElm);
                        if (progress != null) {
                                if (status == Job.Status.SUCCESS || job.getStatus() == Job.Status.FAIL)
                                        progress = BigDecimal.ONE;
                                messagesElm.setAttribute("progress", Float.toString(progress.floatValue()));
                        }
                        if (messages != null && messages.size() > 0) {
                                if (messagesNewerThan >= 0)
                                        messagesElm.setAttribute("msgSeq", ""+messagesNewerThan);
                                for (Message message : messages) {
                                        addMessage(message, true, messagesElm);
                                }
                        }
                }
                
                status = job.getStatus();
                if (status == Job.Status.SUCCESS || status == Job.Status.FAIL || status == Job.Status.ERROR) {
                        URI logfileUri = job.getLogFile();
                        if (logfileUri != null) {
                                Element logElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "log");
                                String logHref = baseUrl + Routes.LOG_ROUTE.replaceFirst("\\{id\\}", job.getId().toString());
                                logElm.setAttribute("href", logHref);
                                element.appendChild(logElm);
                        }
                }
                if (this.fullResult && (status == Job.Status.SUCCESS || status == Job.Status.FAIL)) {
                        addResults(element);
                }
        }

        public static void addMessage(Message message, boolean progress, Element parentElem) {
                Document doc = parentElem.getOwnerDocument();
                Element messageElem = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "message");
                messageElem.setAttribute("level", message.getLevel().toString());
                messageElem.setAttribute("sequence", Integer.toString(message.getSequence()));
                messageElem.setAttribute("content", message.getText());
                messageElem.setAttribute("timeStamp", Long.toString(message.getTimeStamp().getTime()));
                      if (message instanceof ProgressMessage) {
                        ProgressMessage jm = (ProgressMessage)message;
                        if (progress) {
                                BigDecimal portion = jm.getPortion();
                                if (portion.compareTo(BigDecimal.ZERO) > 0) {
                                        messageElem.setAttribute("portion", Float.toString(portion.floatValue()));
                                        messageElem.setAttribute("progress", Float.toString(jm.getProgress().floatValue()));
                                } else {
                                        progress = false;
                                }
                        }
                        for (Message m : jm) {
                                addMessage(m, progress, messageElem);
                        }
                }
                parentElem.appendChild(messageElem);
        }

        private void addResults(Element jobElem) {
                //check if there are actual results
                if (this.job.getResults() == null || this.job.getResults().getResults().size() == 0) {
                        return;
                }
                Document doc = jobElem.getOwnerDocument();
                Element resultsElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "results");
                String resultHref = baseUrl + Routes.RESULT_ROUTE.replaceFirst("\\{id\\}", job.getId().toString());
                resultsElm.setAttribute("href", resultHref);
                resultsElm.setAttribute("mime-type", "application/zip");
                jobElem.appendChild(resultsElm);
                //ports
                for (String portName : this.job.getResults().getPorts()) {
                        ScriptPort port = job.getScript().getOutputPort(portName);
                        if (this.onlyPrimaries && !port.isPrimary()) {
                                continue;
                        }
                        Element portResultElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "result");
                        portResultElm.setAttribute("href", String.format("%s/port/%s",resultHref, portName));
                        portResultElm.setAttribute("mime-type", "application/zip");
                        /**
                         * Note that this attribute does not really have a meaning anymore now that
                         * all results come from ports, but it is kept for backward compatibility.
                         */
                        portResultElm.setAttribute("from", "port");
                        portResultElm.setAttribute("name", portName);
                        portResultElm.setAttribute("nicename", port.getNiceName());
                        String desc = port.getDescription();
                        if (desc != null && !"".equals(desc)) {
                                portResultElm.setAttribute("desc", desc);
                        }
                        resultsElm.appendChild(portResultElm);
                        for (JobResult result : this.job.getResults().getResults(portName)) {
                                Element resultElm= doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "result");
                                resultElm.setAttribute("href", String.format("%s/port/%s/idx/%s",resultHref, portName, result.getIdx()));
                                if(result.getMediaType()!= null && !result.getMediaType().isEmpty()){
                                        resultElm.setAttribute("mime-type", result.getMediaType());
                                }
                                if ( this.localPaths){
                                        resultElm.setAttribute("file", result.getPath().toURI().toString());
                                }
                                resultElm.setAttribute("size",
                                                String.format("%s", result.getSize()));
                                portResultElm.appendChild(resultElm); 
                        }
                }
        }

        public void withPriority(Priority priority) {
                this.priority = priority;
        }

                public void withQueuePosition(int pos) {
                        this.queuePosition = pos;
                        
                }
}
