package org.daisy.pipeline.webserviceutils.xml;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.webserviceutils.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobsXmlWriter {
	
	Iterable<? extends Job> jobs = null;
	private static Logger logger = LoggerFactory.getLogger(JobsXmlWriter.class.getName());
        private boolean localPaths=false; 
	
	public JobsXmlWriter(Iterable<? extends Job> jobs) {
		this.jobs = jobs;
	}


	
	public Document getXmlDocument() {
		if (jobs == null) {
			logger.warn("Could not create XML for null jobs");
			return null;
		}
		return jobsToXml(jobs);
	}
	
	private Document jobsToXml(Iterable<? extends Job> jobs) {
		String baseUri = new Routes().getBaseUri();
		Document doc = XmlUtils.createDom("jobs");
		Element jobsElm = doc.getDocumentElement();
		jobsElm.setAttribute("href", baseUri + Routes.JOBS_ROUTE);
		
		for (Job job : jobs) {
			JobXmlWriter writer = new JobXmlWriter(job);
                        writer.withFullResults(true);
                        writer.withOnlyPrimaries(true);
                        if(this.localPaths){
                                writer.withLocalPaths();
                        }
			writer.addAsElementChild(jobsElm);
		}
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.JOBS_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}

		return doc;
	}

        public void withLocalPaths(){
                this.localPaths=true;
        }
}
