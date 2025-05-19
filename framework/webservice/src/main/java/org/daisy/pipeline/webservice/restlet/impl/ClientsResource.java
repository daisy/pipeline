package org.daisy.pipeline.webservice.restlet.impl;

import java.io.IOException;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webservice.request.ClientRequest;
import org.daisy.pipeline.webservice.restlet.AdminResource;
import org.daisy.pipeline.webservice.xml.ClientXmlWriter;
import org.daisy.pipeline.webservice.xml.ClientsXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class ClientsResource extends AdminResource {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ClientsResource.class.getName());

	@Override
    public void doInit() {
		super.doInit();
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
    	ClientsXmlWriter writer = new ClientsXmlWriter(getStorage().getClientStorage().getAll(),
    	                                               getRequest().getRootRef().toString());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());

		logResponse(dom);
		return dom;
    }

	@Post
	public Representation createResource(Representation representation) {
		logRequest();
		maybeEnableCORS();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		ClientRequest req; {
			if (representation != null) {
				String xml = null;
				try {
					xml = representation.getText();
					req = ClientRequest.fromXML(xml);
				} catch (IOException|IllegalArgumentException e) {
					logger.error("Bad request", e);
					if (xml != null && logger.isDebugEnabled())
						logger.debug("Request XML: " + xml);
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation(e.getMessage());
				}
			} else {
				try {
					req = ClientRequest.fromQuery(getQuery());
				} catch (IllegalArgumentException e) {
					logger.error("Bad request", e);
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation(e.getMessage());
				}
			}
		}
		Optional<Client> newClient = getStorage().getClientStorage().addClient(
			req.getId(),
			req.getSecret(),
			req.getRole(),
			req.getContact());
		if (!newClient.isPresent()) {
			// the client ID was probably not unique
			logger.debug("Client id not unique");
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation("Client id not unique");
		}
		setStatus(Status.SUCCESS_CREATED);
		ClientXmlWriter writer = new ClientXmlWriter(newClient.get(), getRequest().getRootRef().toString());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		logResponse(dom);
		return dom;
	}
}
