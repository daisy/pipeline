package org.daisy.pipeline.webservice.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webserviceutils.xml.ClientXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.ClientsXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlValidator;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
    	if (!isAuthorized()) {
    		setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    		return null;
    	}

    	setStatus(Status.SUCCESS_OK);
    	ClientsXmlWriter writer = XmlWriterFactory.createXmlWriterForClients(webservice().getStorage().getClientStorage().getAll());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());

		return dom;
    }

    @Post
    public Representation createResource(Representation representation) {
	    if (!isAuthorized()) {
		    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
		    return null;
	    }

	    if (representation == null) {
		    // POST request with no entity.
		    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    return this.getErrorRepresentation("Post request with no entity");
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
		    this.getErrorRepresentation(e);
	    } catch (ParserConfigurationException e) {
		    logger.error(e.getMessage());
		    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    this.getErrorRepresentation(e);
	    } catch (SAXException e) {
		    logger.error(e.getMessage());
		    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    this.getErrorRepresentation(e);
	    }

	    boolean isValid = XmlValidator.validate(doc, XmlValidator.CLIENT_SCHEMA_URL);

	    if (!isValid) {
		    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    return this.getErrorRepresentation("Response XML not valid");
	    }

	    Element root = doc.getDocumentElement();
	    Optional<Client> newClient = webservice().getStorage().getClientStorage().addClient(root.getAttribute("id"),
			    root.getAttribute("secret"), Client.Role.valueOf(root
				    .getAttribute("role")), root.getAttribute("contact"));

	    if (!newClient.isPresent()) {
		    // the client ID was probably not unique
		    logger.debug("Client id not unique");
		    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    return this.getErrorRepresentation("Client id not unique");
	    }

	    setStatus(Status.SUCCESS_CREATED);
	    ClientXmlWriter writer = XmlWriterFactory.createXmlWriterForClient(newClient.get());
	    DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
	    return dom;
    }
}
