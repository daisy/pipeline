package org.daisy.pipeline.webservice.impl;

import java.util.LinkedList;

import org.daisy.common.properties.Property;
import org.daisy.common.properties.PropertyTracker;

import org.daisy.pipeline.webserviceutils.xml.PropertiesXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

import org.restlet.ext.xml.DomRepresentation;

import org.restlet.representation.Representation;

import org.restlet.resource.Get;

public class PropertiesResource extends AdminResource {

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}
	}
	@Get("xml")
	public Representation getResource() {
		logRequest();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		PropertyTracker tracker = webservice().getPropertyTracker();
		if (tracker==null){
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return this.getErrorRepresentation("Property tracker is null");
		}

		
		LinkedList<Property> l = new LinkedList<Property>(tracker.getProperties());
		PropertiesXmlWriter writer = XmlWriterFactory.createXmlWriterForProperties(l);	
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());
		logResponse(dom);
		return dom;
	}
}
