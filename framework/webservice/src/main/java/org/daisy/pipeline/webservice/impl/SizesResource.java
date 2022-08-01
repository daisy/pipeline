package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.job.JobSize;
import org.daisy.pipeline.webservice.xml.JobsSizeXmlWriter;

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
		logRequest();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		setStatus(Status.SUCCESS_OK);

		JobsSizeXmlWriter writer = new JobsSizeXmlWriter(JobSize.getSizes(webservice().getJobManager(this.getClient()).getJobs()));
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
                                writer.getXmlDocument());
		logResponse(dom);
		return dom;
	}

}
