package org.daisy.pipeline.webservice.xml;

import org.daisy.pipeline.job.JobSize;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobsSizeXmlWriter{

        private final String baseUrl;
        Iterable<JobSize> sizes; 
        private static final Logger logger = LoggerFactory.getLogger(JobsSizeXmlWriter.class);

	/**
	 * @param baseUrl Prefix to be included at the beginning of <code>href</code>
	 *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
	 *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
	 *                absolute paths relative to the domain name.
	 */
	public JobsSizeXmlWriter(Iterable<JobSize> sizes, String baseUrl) {
		this.sizes = sizes;
		this.baseUrl = baseUrl;
	}
	
	public Document getXmlDocument() {
		if (this.sizes== null) {
			logger.warn("Could not create XML for null jobs");
			return null;
		}
		return sizesToXml(this.sizes, baseUrl);
	}
	
	private static Document sizesToXml(Iterable<JobSize> sizes, String baseUrl) {
		Document doc = XmlUtils.createDom("jobSizes");
		Element sizesElm = doc.getDocumentElement();
		sizesElm.setAttribute("href", baseUrl + Routes.SIZES_ROUTE);
		sizesElm.setAttribute("total", String.valueOf(JobSize.getTotal(sizes)));
		
		for (JobSize size : sizes) {
			JobSizeXmlWriter writer = new JobSizeXmlWriter(size);
			writer.addAsElementChild(sizesElm);
		}
		
                if (!XmlValidator.validate(doc, XmlValidator.SIZES_SCHEMA_URL)) {
                        logger.error("INVALID XML:\n" + XmlUtils.nodeToString(doc));
                }

		return doc;
	}
        
}
