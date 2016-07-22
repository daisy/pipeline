package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.webserviceutils.xml.ErrorWriter;

import org.restlet.data.MediaType;

import org.restlet.ext.xml.DomRepresentation;

import org.restlet.representation.Representation;

import org.restlet.resource.ServerResource;

public abstract class GenericResource extends ServerResource {

	protected PipelineWebService webservice() {
		return (PipelineWebService) getApplication();
	}

	protected Representation getErrorRepresentation(Throwable error){
		ErrorWriter.ErrorWriterBuilder builder=new ErrorWriter.ErrorWriterBuilder().withError(error).withUri(this.getStatus().getUri());
		return new DomRepresentation(MediaType.APPLICATION_XML,
				builder.build().getXmlDocument());
	}

	protected Representation getErrorRepresentation(String error){
		ErrorWriter.ErrorWriterBuilder builder=new ErrorWriter.ErrorWriterBuilder().withError(new Throwable(error)).withUri(this.getStatus().getUri());
		return new DomRepresentation(MediaType.APPLICATION_XML,
				builder.build()
				.getXmlDocument());
	}
}
