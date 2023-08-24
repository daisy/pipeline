package org.daisy.pipeline.webservice.restlet.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.SettableProperty;
import org.daisy.pipeline.webservice.restlet.AdminResource;
import org.daisy.pipeline.webservice.xml.PropertyXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PropertyResource extends AdminResource {

	private static Logger logger = LoggerFactory.getLogger(PropertyResource.class.getName());
	private Optional<SettableProperty> property;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}
		String name = (String)getRequestAttributes().get("name");
		for (SettableProperty p : Properties.getSettableProperties()) {
			if (name.equals(p.getName())) {
				property = Optional.of(p);
				break;
			}
		}
		if (property == null)
			property = Optional.empty();
	}

	@Get("xml")
	public Representation getResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		if (!property.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Property not found");
		}
		PropertyXmlWriter writer = new PropertyXmlWriter(property.get(), getRequest().getRootRef().toString(), false);
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		setStatus(Status.SUCCESS_OK);
		if (logger.isDebugEnabled())
			logger.debug(
				XmlUtils.nodeToString(
					new PropertyXmlWriter(property.get(), getRequest().getRootRef().toString(), true).getXmlDocument()));
		return dom;
	}

	@Put
	public Representation putResource(Representation representation) {
		logRequest();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		if (!property.isPresent()) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation("Property not found");
		}
		if (representation == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("PUT request with no data");
		}
		Document doc = null; {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(representation.getText())));
			} catch (IOException|ParserConfigurationException|SAXException e) {
				logger.error(e.getMessage());
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return getErrorRepresentation(e);
			}
		}
		if (!XmlValidator.validate(doc, XmlValidator.PROPERTY_SCHEMA_URL)) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation("Invalid property XML provided");
		}
		Attr val = doc.getDocumentElement().getAttributeNode("value");
		if (val == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation("Invalid property XML provided: 'property' element missing 'value' attribute");
		}
		property.get().setValue(val.getValue());
		PropertyXmlWriter writer = new PropertyXmlWriter(property.get(), getRequest().getRootRef().toString(), false);
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		setStatus(Status.SUCCESS_OK);
		if (logger.isDebugEnabled())
			logger.debug(
				XmlUtils.nodeToString(
					new PropertyXmlWriter(property.get(), getRequest().getRootRef().toString(), true).getXmlDocument()));
		return dom;
	}
}
