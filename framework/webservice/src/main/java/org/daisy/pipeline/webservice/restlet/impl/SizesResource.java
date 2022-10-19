package org.daisy.pipeline.webservice.restlet.impl;

import org.daisy.pipeline.job.JobSize;
import org.daisy.pipeline.webservice.restlet.AdminResource;
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
		maybeEnableCORS();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		setStatus(Status.SUCCESS_OK);

		JobsSizeXmlWriter writer = new JobsSizeXmlWriter(JobSize.getSizes(getJobManager(this.getClient()).getJobs()),
		                                                 getRequest().getRootRef().toString());
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
                                writer.getXmlDocument());
		logResponse(dom);
		return dom;
	}
}