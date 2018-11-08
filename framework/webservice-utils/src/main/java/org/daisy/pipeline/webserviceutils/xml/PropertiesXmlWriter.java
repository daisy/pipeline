package org.daisy.pipeline.webserviceutils.xml;

import java.util.List;
import org.daisy.common.properties.Property;

import org.daisy.pipeline.webserviceutils.Routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertiesXmlWriter {
	private static Logger logger = LoggerFactory.getLogger(PropertiesXmlWriter.class.getName());
	private List<? extends Property> properties;

	/**
	 * Constructs a new instance.
	 *
	 * @param properties The properties for this instance.
	 */
	public PropertiesXmlWriter(List<? extends Property> properties) {
		this.properties = properties;
	}
	public Document getXmlDocument() {
		return propertiesToXmlDoc();
	}
	
	private Document propertiesToXmlDoc() {
		String baseUri = new Routes().getBaseUri();
		Document doc = XmlUtils.createDom("properties");
		Element propsElm = doc.getDocumentElement();
		propsElm.setAttribute("href", baseUri + Routes.PROPERTIES_ROUTE);
		for (Property prop: this.properties) {
			PropertyXmlWriter writer = XmlWriterFactory.createXmlWriterForProperty(prop);
			writer.addAsElementChild(propsElm);
		}
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.PROPERTIES_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}
		return doc;
	}

}
