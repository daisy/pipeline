package org.daisy.pipeline.webservice.restlet.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.SettableProperty;
import org.daisy.pipeline.webservice.restlet.AdminResource;
import org.daisy.pipeline.webservice.xml.PropertiesXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesResource extends AdminResource {

	private static Logger logger = LoggerFactory.getLogger(PropertiesResource.class.getName());

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
		maybeEnableCORS();
		if (!isAuthorized()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		List<SettableProperty> properties = new ArrayList<>(Properties.getSettableProperties());
		PropertiesXmlWriter writer = new PropertiesXmlWriter(properties, getRequest().getRootRef().toString(), false);
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
		setStatus(Status.SUCCESS_OK);
		if (logger.isDebugEnabled())
			logger.debug(
				XmlUtils.nodeToString(
					new PropertiesXmlWriter(properties, getRequest().getRootRef().toString(), true).getXmlDocument()));
		return dom;
	}
}
