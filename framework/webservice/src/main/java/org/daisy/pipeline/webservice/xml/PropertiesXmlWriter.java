package org.daisy.pipeline.webservice.xml;

import java.util.List;

import org.daisy.common.properties.Properties.SettableProperty;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webservice.Routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertiesXmlWriter {

	private static Logger logger = LoggerFactory.getLogger(PropertiesXmlWriter.class.getName());

	private final String baseUrl;
	private final String route;
	private final List<SettableProperty> properties;
	private final Client client;
	private final boolean scrub;

	/**
	 * @param client Retrieve properties specific to the given client. Get global properties if {@code null}.
	 * @param scrub Whether to scrub sensitive data
	 */
	public PropertiesXmlWriter(List<SettableProperty> properties, Client client, String baseUrl, boolean scrub) {
		this(properties, client, baseUrl, Routes.ADMIN_PROPERTIES_ROUTE, scrub);
	}

	public PropertiesXmlWriter(List<SettableProperty> properties, Client client, String baseUrl, String route, boolean scrub) {
		this.properties = properties;
		this.client = client;
		this.baseUrl = baseUrl;
		this.route = route;
		this.scrub = scrub;
	}

	public Document getXmlDocument() {
		Document doc = getXmlDocument(scrub);
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.PROPERTIES_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.nodeToString(scrub ? doc : getXmlDocument(true)));
		}
		return doc;
	}

	private Document getXmlDocument(boolean scrub) {
		Document doc = XmlUtils.createDom("properties");
		Element propsElm = doc.getDocumentElement();
		propsElm.setAttribute("href", baseUrl + route);
		for (SettableProperty p : properties) {
			PropertyXmlWriter writer = new PropertyXmlWriter(p, client, baseUrl, route + "/{id}", scrub);
			writer.addAsElementChild(propsElm);
		}
		return doc;
	}
}
