package org.daisy.pipeline.webservice.xml;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobsXmlWriter {
	
	private final String baseUrl;
	Iterable<? extends Job> jobs = null;
	private static Logger logger = LoggerFactory.getLogger(JobsXmlWriter.class.getName());
        private boolean localPaths=false; 

	/**
	 * @param baseUrl Prefix to be included at the beginning of <code>href</code>
	 *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
	 *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
	 *                absolute paths relative to the domain name.
	 */
	public JobsXmlWriter(Iterable<? extends Job> jobs, String baseUrl) {
		this.jobs = jobs;
		this.baseUrl = baseUrl;
	}


	public Document getXmlDocument() {
		if (jobs == null) {
			logger.warn("Could not create XML for null jobs");
			return null;
		}
		return jobsToXml(jobs);
	}
	
	private Document jobsToXml(Iterable<? extends Job> jobs) {
		Document doc = XmlUtils.createDom("jobs");
		Element jobsElm = doc.getDocumentElement();
		jobsElm.setAttribute("href", baseUrl + Routes.JOBS_ROUTE);
		
		for (Job job : jobs) {
			JobXmlWriter writer = new JobXmlWriter(job, baseUrl);
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
