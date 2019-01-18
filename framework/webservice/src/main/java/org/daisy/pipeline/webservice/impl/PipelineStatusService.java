package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.webserviceutils.xml.ErrorWriter.ErrorWriterBuilder;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Resource;
import org.restlet.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineStatusService extends StatusService {
	private static Logger logger = LoggerFactory
			.getLogger(PipelineStatusService.class);

	@Override
	public Representation getRepresentation(Status status, Request request,
			Response response) {
		logger.debug(status.toString());
		ErrorWriterBuilder builder=new ErrorWriterBuilder().withUri(status.getUri());
		if (status.getThrowable()!=null) {
			logger.info("Overriding error representation: "
					+ status.getThrowable().getMessage());
			builder.withError(status.getThrowable());
			
		}
		return new DomRepresentation(MediaType.APPLICATION_XML,
				builder.build().getXmlDocument());

	}

	@Override
	public Status getStatus(Throwable except, Request req, Response res) {

		logger.info("Error caught from application: " + except.getMessage());
		return new Status(Status.SERVER_ERROR_INTERNAL.getCode(), except, "",
				"", req.getOriginalRef().getQuery());
	}

	@Override
	public Status getStatus(Throwable except, Resource resource) {

		logger.info("Error caught from application(resource): "
				+ except.getMessage());
		return new Status(Status.SERVER_ERROR_INTERNAL.getCode(), except, "",
				"", resource.getRequest().getOriginalRef().toString());
	}

}
