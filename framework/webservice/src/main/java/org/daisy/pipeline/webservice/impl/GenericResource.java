package org.daisy.pipeline.webservice.impl;

import java.io.StringWriter;

import org.daisy.pipeline.webserviceutils.xml.ErrorWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.Request;
import org.restlet.resource.ServerResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericResource extends ServerResource {

	private static Logger logger = LoggerFactory.getLogger(GenericResource.class);

	protected PipelineWebService webservice() {
		return (PipelineWebService) getApplication();
	}

	protected Representation getErrorRepresentation(Throwable error){
		logger.debug(null, error);
		ErrorWriter.ErrorWriterBuilder builder=new ErrorWriter.ErrorWriterBuilder().withError(error).withUri(this.getStatus().getUri());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				builder.build().getXmlDocument());
		logResponse(dom);
		return dom;
	}

	protected Representation getErrorRepresentation(String error){
		logger.debug(error);
		ErrorWriter.ErrorWriterBuilder builder=new ErrorWriter.ErrorWriterBuilder().withError(new Throwable(error)).withUri(this.getStatus().getUri());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				builder.build()
				.getXmlDocument());
		logResponse(dom);
		return dom;
	}
	
	@Override
	public void doCatch(Throwable e) {
		logger.debug(null, e);
		super.doCatch(e);
	}
	
	@Override
	public void setStatus(Status status) {
		logStatus(status);
		super.setStatus(status);
	}
	
	protected void logResponse(DomRepresentation dom) {
		if (logger.isDebugEnabled())
			try {
				StringWriter w = new StringWriter();
				dom.write(w);
				logger.debug(w.getBuffer().toString());
			} catch (java.io.IOException e) {}
	}
	
	protected void logRequest() {
		Request req = getRequest();
		logger.debug(req.getMethod()+" "+req.getResourceRef().getPath());
	}

	private void logStatus(Status status) {
		logger.debug(status.toString());
	}
}
