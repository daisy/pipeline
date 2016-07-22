package org.daisy.pipeline.webservice.impl;


//import static org.daisy.pipeline.webservice.AuthenticatedResource.logger;

import org.daisy.pipeline.job.JobSize;
import org.daisy.pipeline.webserviceutils.xml.JobsSizeXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlUtils;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;


public class SizesResource extends AdminResource {
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthorized()) {
			return;
		}
	}

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Get("xml")
	public Representation getResource() {
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		setStatus(Status.SUCCESS_OK);

		JobsSizeXmlWriter writer = XmlWriterFactory.createXmlWriterForJobSizes(JobSize.getSizes(webservice().getJobManager(this.getClient()).getJobs()));
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
                                writer.getXmlDocument());
		return dom;
	}

}
