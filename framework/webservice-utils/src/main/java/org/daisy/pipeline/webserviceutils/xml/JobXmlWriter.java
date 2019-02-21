package org.daisy.pipeline.webserviceutils.xml;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.event.ProgressMessage;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.webserviceutils.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobXmlWriter {
        
        private Job job = null;
        private List<Message> messages = null;
        private long messagesNewerThan = -1;
        private BigDecimal progress = null;
        private boolean scriptDetails = false;
        private boolean fullResult=false;
        private boolean localPaths=false; 
        private boolean onlyPrimaries=false; 

        private Job.Status statusOverWrite = null;
        private int queuePosition = -1;
        private static Logger logger = LoggerFactory.getLogger(JobXmlWriter.class
                        .getName());

        private static HashSet<Level> MSG_LEVELS = new HashSet<Level>();
        static {
                MSG_LEVELS.add(Level.WARNING);
                MSG_LEVELS.add(Level.INFO);
                MSG_LEVELS.add(Level.ERROR);
        }

        public JobXmlWriter(Job job) {
                this.job = job;
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
                MessageAccessor accessor = job.getContext().getMonitor().getMessageAccessor();
                if (accessor != null) {
                        progress = accessor.getProgress();
                        messages = accessor.createFilter().filterLevels(MSG_LEVELS).getMessages();
                }
                return this;
        }

        public JobXmlWriter withNewMessages(int newerThan) {
                MessageAccessor accessor = job.getContext().getMonitor().getMessageAccessor();
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
                        logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
                }

                return doc;
        }
        
        private void addElementData(Job job, Element element) {
                Document doc = element.getOwnerDocument();
                String baseUri = new Routes().getBaseUri();
                Job.Status status = (this.statusOverWrite==null)?job.getStatus():this.statusOverWrite;
                String jobHref = baseUri + Routes.JOB_ROUTE.replaceFirst("\\{id\\}", job.getId().toString());
                
                element.setAttribute("id", job.getId().toString());
                element.setAttribute("href", jobHref);
                element.setAttribute("status", status.toString());
                element.setAttribute("priority", job.getPriority().toString().toLowerCase());
                if (this.queuePosition != -1){
                        element.setAttribute("queue-position",String.format("%d",this.queuePosition));
                }

                if(!job.getContext().getName().isEmpty()){
                        Element nicenameElem= doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "nicename");
                        nicenameElem.setTextContent(job.getContext().getName());
                        element.appendChild(nicenameElem);
                }
                if(!job.getContext().getBatchId().toString().isEmpty()){
                        Element batchId= doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "batchId");
                        batchId.setTextContent(job.getContext().getBatchId().toString());
                        element.appendChild(batchId);
                }

                if (scriptDetails) {
                        XProcScript script=job.getContext().getScript();
                        //return if no script was loadeded
                        if(script.getDescriptor()!=null){
                                ScriptXmlWriter writer = XmlWriterFactory.createXmlWriterForScript(script);
                                writer.addAsElementChild(element);
                        }
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
                
                if (job.getStatus() == Job.Status.SUCCESS || job.getStatus() == Job.Status.FAIL) {
                        Element logElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "log");
                        String logHref = baseUri + Routes.LOG_ROUTE.replaceFirst("\\{id\\}", job.getId().toString());
                        logElm.setAttribute("href", logHref);
                        element.appendChild(logElm);
                        if(this.fullResult)
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
                if (this.job.getContext().getResults()==null || this.job.getContext().getResults().getResults().size() == 0){
                        return;
                }
                Document doc = jobElem.getOwnerDocument();
                String baseUri = new Routes().getBaseUri();
                Element resultsElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "results");
                String resultHref = baseUri + Routes.RESULT_ROUTE.replaceFirst("\\{id\\}", job.getId().toString());
                resultsElm.setAttribute("href", resultHref);
                resultsElm.setAttribute("mime-type", "application/zip");
                jobElem.appendChild(resultsElm);
                //ports
                for (String port : this.job.getContext().getResults().getPorts()) {
                        if (this.onlyPrimaries && !job.getContext().getScript().getPortMetadata(port).isPrimary()){
                                continue;
                        }
                        Element portResultElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "result");
                        portResultElm.setAttribute("href", String.format("%s/port/%s",resultHref,port));
                        portResultElm.setAttribute("mime-type", "application/zip");
                        portResultElm.setAttribute("from", "port");
                        portResultElm.setAttribute("name", port);
                        portResultElm.setAttribute("nicename", job.getContext().getScript().getPortMetadata(port).getNiceName());
                        resultsElm.appendChild(portResultElm);
                        for(JobResult result: this.job.getContext().getResults().getResults(port)){
                                Element resultElm= doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "result");
                                resultElm.setAttribute("href", String.format("%s/port/%s/idx/%s",resultHref,port,result.getIdx()));
                                if(result.getMediaType()!= null && !result.getMediaType().isEmpty()){
                                        resultElm.setAttribute("mime-type", result.getMediaType());
                                }
                                if ( this.localPaths){
                                        resultElm.setAttribute("file",result.getPath().toString());
                                }
                                resultElm.setAttribute("size",
                                                String.format("%s", result.getSize()));
                                portResultElm.appendChild(resultElm); 
                        }
                }


                for(QName option: this.job.getContext().getResults().getOptions()){
                        XProcOptionMetadata meta=job.getContext().getScript().getOptionMetadata(option);
                        if ( this.onlyPrimaries&&  (meta==null || !meta.isPrimary())){
                                continue;
                        }
                        Element optionResultElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "result");
                        optionResultElm.setAttribute("href", String.format("%s/option/%s",resultHref,option));
                        optionResultElm.setAttribute("mime-type", "application/zip");
                        optionResultElm.setAttribute("from", "option");
                        optionResultElm.setAttribute("name", option.toString());
                        //in case the script was deleted
                        if (meta!=null){
                                optionResultElm.setAttribute("nicename", job.getContext().getScript().getOptionMetadata(option).getNiceName());
                        }
                        resultsElm.appendChild(optionResultElm);
                        for(JobResult result: this.job.getContext().getResults().getResults(option)){
                                Element resultElm= doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "result");
                                resultElm.setAttribute("href", String.format("%s/option/%s/idx/%s",resultHref,option,result.getIdx()));
                                if(result.getMediaType()!= null && !result.getMediaType().isEmpty()){
                                        resultElm.setAttribute("mime-type", result.getMediaType());
                                }
                                if ( this.localPaths){
                                        resultElm.setAttribute("file",result.getPath().toString());
                                }
                                resultElm.setAttribute("size",
                                                String.format("%s", result.getSize()));
                                optionResultElm.appendChild(resultElm);
                        }
                }
        }

                public void withQueuePosition(int pos) {
                        this.queuePosition = pos;
                        
                }
}
