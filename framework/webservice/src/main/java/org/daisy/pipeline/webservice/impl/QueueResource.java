package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.webservice.xml.QueueXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class QueueResource extends AuthenticatedResource {

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
	}

	/**
	 * List the jobs, their final priorities and their times
	 *
	 * @return the resource
	 */
	@Get("xml")
	public Representation getResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		setStatus(Status.SUCCESS_OK);
		QueueXmlWriter writer = new QueueXmlWriter(
			webservice().getJobManager(getClient()).getExecutionQueue(),
			getRequest().getRootRef().toString());
                DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
                                writer.getXmlDocument());
		logResponse(dom);
		return dom;
	}

}
