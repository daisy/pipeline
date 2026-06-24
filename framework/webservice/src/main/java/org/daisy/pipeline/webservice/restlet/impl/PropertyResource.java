package org.daisy.pipeline.webservice.restlet.impl;

import java.io.IOException;
import java.util.Optional;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.SettableProperty;
import org.daisy.pipeline.webservice.Routes;
import org.daisy.pipeline.webservice.request.PropertyRequest;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.xml.PropertyXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyResource extends AuthenticatedResource {

	private static Logger logger = LoggerFactory.getLogger(PropertyResource.class.getName());
	private Optional<SettableProperty> property;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
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
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		if (!property.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Property not found");
		}
		if (!property.get().isClientLevel()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return getErrorRepresentation("Property not settable by non-admin clients");
		}
		PropertyXmlWriter writer = new PropertyXmlWriter(
			property.get(), getClient(), getRequest().getRootRef().toString(), Routes.PROPERTY_ROUTE, false);
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		setStatus(Status.SUCCESS_OK);
		if (logger.isDebugEnabled())
			logger.debug(
				XmlUtils.nodeToString(
					new PropertyXmlWriter(
						property.get(), getClient(), getRequest().getRootRef().toString(), Routes.PROPERTY_ROUTE, true
					).getXmlDocument()));
		return dom;
	}

	@Put
	public Representation putResource(Representation representation) {
		logRequest();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		if (!property.isPresent()) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation("Property not found");
		}
		if (!property.get().isClientLevel()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return getErrorRepresentation("Property not settable by non-admin clients");
		}
		String name = property.get().getName();
		PropertyRequest req; {
			if (representation != null) {
				String xml = null;
				try {
					xml = representation.getText();
					req = PropertyRequest.fromXML(xml, name);
				} catch (IOException|IllegalArgumentException e) {
					logger.error("Bad request", e);
					if (xml != null && logger.isDebugEnabled())
						logger.debug("Request XML: " + xml);
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation(e.getMessage());
				}
			} else {
				try {
					req = PropertyRequest.fromQuery(getQuery(), name);
				} catch (IllegalArgumentException e) {
					logger.error("Bad request", e);
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation(e.getMessage());
				}
			}
		}
		property.get().setValue(req.getValue());
		PropertyXmlWriter writer = new PropertyXmlWriter(
			property.get(), getClient(), getRequest().getRootRef().toString(), Routes.PROPERTY_ROUTE, false);
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		setStatus(Status.SUCCESS_OK);
		if (logger.isDebugEnabled())
			logger.debug(
				XmlUtils.nodeToString(
					new PropertyXmlWriter(
						property.get(), getClient(), getRequest().getRootRef().toString(), Routes.PROPERTY_ROUTE, true
					).getXmlDocument()));
		return dom;
	}
}
