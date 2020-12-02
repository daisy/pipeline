package org.daisy.pipeline.webservice.xml;

import org.daisy.pipeline.job.JobSize;
import org.daisy.pipeline.webservice.Routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobsSizeXmlWriter{
        Iterable<JobSize> sizes; 
        private static final Logger logger = LoggerFactory.getLogger(JobsSizeXmlWriter.class);
	public JobsSizeXmlWriter(Iterable<JobSize> sizes) {
		this.sizes = sizes;
	}
	
	public Document getXmlDocument() {
		if (this.sizes== null) {
			logger.warn("Could not create XML for null jobs");
			return null;
		}
		return sizesToXml(this.sizes);
	}
	
	private static Document sizesToXml(Iterable<JobSize> sizes) {
		String baseUri = new Routes().getBaseUri();
		Document doc = XmlUtils.createDom("jobSizes");
		Element sizesElm = doc.getDocumentElement();
		sizesElm.setAttribute("href", baseUri + Routes.SIZES_ROUTE);
		sizesElm.setAttribute("total", String.valueOf(JobSize.getTotal(sizes)));
		
		for (JobSize size : sizes) {
			JobSizeXmlWriter writer = new JobSizeXmlWriter(size);
			writer.addAsElementChild(sizesElm);
		}
		
                if (!XmlValidator.validate(doc, XmlValidator.SIZES_SCHEMA_URL)) {
                        logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
                }

		return doc;
	}
        
}
