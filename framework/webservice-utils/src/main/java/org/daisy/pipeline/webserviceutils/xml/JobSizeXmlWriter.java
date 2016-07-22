package org.daisy.pipeline.webserviceutils.xml;

import org.daisy.pipeline.job.JobSize;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobSizeXmlWriter   {
                
        private JobSize size;

        /**
         * @param size
         */
        public JobSizeXmlWriter(JobSize size) {
                this.size = size;
        }

        public Document getXmlDocument(){
                Document doc = XmlUtils.createDom("jobSize");
                Element jobSizeElm = doc.getDocumentElement();
                addElementData(size, jobSizeElm);
                
                // for debugging only
                //if (!XmlValidator.validate(doc, XmlValidator.JOBSIZE_SCHEMA_URL)) {
                        //logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
                //}

                return doc;
        }

        public void addAsElementChild(Element parent) {
                Document doc = parent.getOwnerDocument();
                Element jobSizeElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "jobSize");
                this.addElementData(size, jobSizeElm);
                parent.appendChild(jobSizeElm);
        }
        private void addElementData(JobSize size, Element element) {
                element.setAttribute("id",size.getJob().getId().toString());
                element.setAttribute("log",String.valueOf(size.getLogSize()));
                element.setAttribute("context",String.valueOf(size.getContextSize()));
                element.setAttribute("output",String.valueOf(size.getOutputSize()));
                

        }
}
