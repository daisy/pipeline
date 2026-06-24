package org.daisy.pipeline.webservice.restlet.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Collections2;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.SettableProperty;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.Routes;
import org.daisy.pipeline.webservice.xml.PropertiesXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesResource extends AuthenticatedResource {

	private static Logger logger = LoggerFactory.getLogger(PropertiesResource.class.getName());

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}
	}

	@Get("xml")
	public Representation getResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		List<SettableProperty> properties = new ArrayList<>(
			Collections2.filter(Properties.getSettableProperties(), SettableProperty::isClientLevel));
		PropertiesXmlWriter writer = new PropertiesXmlWriter(
			properties, getClient(), getRequest().getRootRef().toString(), Routes.PROPERTIES_ROUTE, false);
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		setStatus(Status.SUCCESS_OK);
		if (logger.isDebugEnabled())
			logger.debug(
				XmlUtils.nodeToString(
					new PropertiesXmlWriter(
						properties, getClient(), getRequest().getRootRef().toString(), Routes.PROPERTIES_ROUTE, true
					).getXmlDocument()));
		return dom;
	}
}
