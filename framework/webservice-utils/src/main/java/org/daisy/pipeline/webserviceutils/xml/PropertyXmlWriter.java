package org.daisy.pipeline.webserviceutils.xml;

import org.daisy.common.properties.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertyXmlWriter {
	private Property prop;

	private static Logger logger = LoggerFactory.getLogger(PropertiesXmlWriter.class);
	/**
	 * Constructs a new instance.
	 *
	 * @param prop The prop for this instance.
	 */
	public PropertyXmlWriter(Property prop) {
		this.prop = prop;
	}
	public Document getXmlDocument() {
		if (prop== null) {
			logger.warn("Could not generate XML for null prop");
			return null;
		}
		return propertyToXmlDocument(prop);
	}

	private static Document propertyToXmlDocument(Property property) {
		Document doc = XmlUtils.createDom("property");
		Element rootElm = doc.getDocumentElement();
		addElementData(property, rootElm);
		
		// for debugging only
//		if (!XmlValidator.validate(doc, XmlValidator.CLIENT_SCHEMA_URL)) {
//			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
//		}
		return doc;
	}
	
	private static void addElementData(Property property, Element element) {

		element.setAttribute("name", property.getPropertyName());
		element.setAttribute("value",property.getValue()); 
		element.setAttribute("bundleName", property.getBundleName());
		element.setAttribute("bundleId",property.getBundleId()+"");
	}
				
	public void addAsElementChild(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element propElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "property");
		addElementData(prop, propElm);
		parent.appendChild(propElm);
	}
}
