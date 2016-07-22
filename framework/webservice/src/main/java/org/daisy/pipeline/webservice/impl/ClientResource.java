package org.daisy.pipeline.webservice.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webserviceutils.xml.ClientXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlValidator;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
		client = webservice().getStorage().getClientStorage().get(idParam);
	}

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Get("xml")
	public Representation getResource() {
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		if (!this.client.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Client not found");	
		}

		setStatus(Status.SUCCESS_OK);
		ClientXmlWriter writer = XmlWriterFactory.createXmlWriterForClient(client.get());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());
		return dom;
	}

	/**
	 * Delete resource.
	 */
	@Delete
	public void deleteResource() {
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}

		if (!client.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}


		if (webservice().getStorage().getClientStorage().delete(client.get().getId())) {
			setStatus(Status.SUCCESS_NO_CONTENT);
		}
		else {

			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	@Put
	public Representation putResource(Representation representation) {

		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		// our PUT method won't create a client, just replace information for an existing client
		if (!client.isPresent()) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation("Client not found, put method won't create a new client, it will just update an exisiting one");
		}

		if (representation == null) {
			// PUT request with no entity.
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("PUT request with no data");
		}

		Document doc = null;

		String s;
		try {
			s = representation.getText();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(s));
			doc = builder.parse(is);
		} catch (IOException e) {
			logger.error(e.getMessage());
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation(e);

		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation(e);

		} catch (SAXException e) {
			logger.error(e.getMessage());
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation(e);
		}


		boolean isValid = XmlValidator.validate(doc, XmlValidator.CLIENT_SCHEMA_URL);

		if (!isValid) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation("Response xml not valid");
			
		}

		Element root = doc.getDocumentElement();
		// TODO SET PRIORITY 
		Optional<Client> updated=webservice().getStorage().getClientStorage().update(this.client.get().getId(),root.getAttribute("secret"),Client.Role.valueOf(root.getAttribute("role")),root.getAttribute("contact"),Priority.MEDIUM);
                if(!updated.isPresent()){
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation("Something prevented this client to be updated");

                }

		setStatus(Status.SUCCESS_OK);
		ClientXmlWriter writer = XmlWriterFactory.createXmlWriterForClient(updated.get());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());
		return dom;

	}
}
