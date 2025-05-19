package org.daisy.pipeline.webservice.restlet.impl;

import java.io.IOException;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webservice.request.ClientRequest;
import org.daisy.pipeline.webservice.restlet.AdminResource;
import org.daisy.pipeline.webservice.xml.ClientXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class ClientResource extends AdminResource {
	private Optional<Client> client;

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ClientResource.class.getName());

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthorized()) {
			return;
		}
		String idParam = (String) getRequestAttributes().get("id");
		client = getStorage().getClientStorage().get(idParam);
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

		if (!this.client.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Client not found");	
		}

		setStatus(Status.SUCCESS_OK);
		ClientXmlWriter writer = new ClientXmlWriter(client.get(), getRequest().getRootRef().toString());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());
		logResponse(dom);
		return dom;
	}

	/**
	 * Delete resource.
	 */
	@Delete
	public void deleteResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}

		if (!client.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}


		if (getStorage().getClientStorage().delete(client.get().getId())) {
			setStatus(Status.SUCCESS_NO_CONTENT);
		}
		else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	@Put
	public Representation putResource(Representation representation) {
		logRequest();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		// our PUT method won't create a client, just replace information for an existing client
		if (!client.isPresent()) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation(
				"Client not found, put method won't create a new client, it will just update an exisiting one");
		}
		String id = client.get().getId();
		ClientRequest req; {
			if (representation != null) {
				String xml = null;
				try {
					xml = representation.getText();
					req = ClientRequest.fromXML(xml, id);
				} catch (IOException|IllegalArgumentException e) {
					logger.error("Bad request", e);
					if (xml != null && logger.isDebugEnabled())
						logger.debug("Request XML: " + xml);
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation(e.getMessage());
				}
			} else {
				try {
					req = ClientRequest.fromQuery(getQuery(), id);
				} catch (IllegalArgumentException e) {
					logger.error("Bad request", e);
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation(e.getMessage());
				}
			}
		}
		// TODO SET PRIORITY
		Optional<Client> updated = getStorage().getClientStorage().update(
			id,
			req.getSecret(),
			req.getRole(),
			req.getContact(),
			Priority.MEDIUM);
		if (!updated.isPresent()) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation("Something prevented this client to be updated");
		}
		setStatus(Status.SUCCESS_OK);
		ClientXmlWriter writer = new ClientXmlWriter(updated.get(), getRequest().getRootRef().toString());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		logResponse(dom);
		return dom;
	}
}
