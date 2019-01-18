package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.webserviceutils.xml.AliveXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class AliveResource extends AuthenticatedResource{
	@Override
    public void doInit() {
		
	}
	
	 /**
     * Gets the resource.
     *
     * @return the resource
     */
    @Get("xml")
    public Representation getResource() {
    	logRequest();

    	setStatus(Status.SUCCESS_OK);
    	AliveXmlWriter writer = XmlWriterFactory.createXmlWriter(); 
    	DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());
		logResponse(dom);
		return dom;
    }

}
