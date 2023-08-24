package org.daisy.pipeline.webservice.xml;

import org.daisy.common.properties.Properties.SettableProperty;
import org.daisy.pipeline.webservice.Routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertyXmlWriter {

	private static Logger logger = LoggerFactory.getLogger(PropertyXmlWriter.class);

	private final String baseUrl;
	private final SettableProperty property;
	private final boolean scrub;

	/**
	 * @param scrub Whether to scrub sensitive data
	 */
	public PropertyXmlWriter(SettableProperty property, String baseUrl, boolean scrub) {
		this.property = property;
		this.baseUrl = baseUrl;
		this.scrub = scrub;
	}

	public Document getXmlDocument() {
		Document doc = getXmlDocument(scrub);
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.PROPERTY_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.nodeToString(scrub ? doc : getXmlDocument(true)));
		}
		return doc;
	}

	private Document getXmlDocument(boolean scrub) {
		Document doc = XmlUtils.createDom("property");
		addElementData(doc.getDocumentElement());
		return doc;
	}

	// instead of creating a standalone XML document, add an element to an existing document
	void addAsElementChild(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element propertyElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "property");
		addElementData(propertyElm);
		parent.appendChild(propertyElm);
	}

	private void addElementData(Element element) {
		element.setAttribute("href", baseUrl + Routes.PROPERTY_ROUTE.replaceFirst("\\{name\\}", property.getName()));
		element.setAttribute("name", property.getName());
		String val = property.getValue();
		if (val != null)
			element.setAttribute("value", scrub && property.isSensitive() ? "***" : val);
		String description = property.getDescription();
		if (description != null)
			element.setAttribute("desc", description);
	}
}
