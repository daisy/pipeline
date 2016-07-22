package org.daisy.pipeline.webserviceutils.xml;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webserviceutils.Routes;
import org.daisy.pipeline.webserviceutils.xml.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClientXmlWriter {
	
	private Client client = null;
	private static Logger logger = LoggerFactory.getLogger(ClientXmlWriter.class);

	public ClientXmlWriter(Client client) {
		this.client = client;
	}
	
	public Document getXmlDocument() {
		if (client == null) {
			logger.warn("Could not generate XML for null client");
			return null;
		}
		return clientToXmlDocument(client);
	}
	
	// instead of getting a document representation, add an element representation to an existing document
	public void addAsElementChild(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element clientElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "client");
		addElementData(client, clientElm);
		parent.appendChild(clientElm);
	}
	
	private static Document clientToXmlDocument(Client client) {
		Document doc = XmlUtils.createDom("client");
		Element rootElm = doc.getDocumentElement();
		addElementData(client, rootElm);
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.CLIENT_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}
		return doc;
	}
	
	private static void addElementData(Client client, Element element) {
		String baseUri = new Routes().getBaseUri();
		String clientHref = baseUri + Routes.CLIENT_ROUTE.replaceFirst("\\{id\\}", client.getId());

		element.setAttribute("id", client.getId());
		element.setAttribute("href", clientHref);
		element.setAttribute("secret", client.getSecret());
		element.setAttribute("role", client.getRole().toString());
		element.setAttribute("contact", client.getContactInfo());
	}
}
