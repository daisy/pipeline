package org.daisy.pipeline.webservice.xml;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClientXmlWriter {
	
	private final String baseUrl;
	private Client client = null;
	private static Logger logger = LoggerFactory.getLogger(ClientXmlWriter.class);

	/**
	 * @param baseUrl Prefix to be included at the beginning of <code>href</code>
	 *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
	 *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
	 *                absolute paths relative to the domain name.
	 */
	public ClientXmlWriter(Client client, String baseUrl) {
		this.client = client;
		this.baseUrl = baseUrl;
	}
	
	public Document getXmlDocument() {
		if (client == null) {
			logger.warn("Could not generate XML for null client");
			return null;
		}
		return clientToXmlDocument(client, baseUrl);
	}
	
	// instead of getting a document representation, add an element representation to an existing document
	public void addAsElementChild(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element clientElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "client");
		addElementData(client, baseUrl, clientElm);
		parent.appendChild(clientElm);
	}
	
	private static Document clientToXmlDocument(Client client, String baseUrl) {
		Document doc = XmlUtils.createDom("client");
		Element rootElm = doc.getDocumentElement();
		addElementData(client, baseUrl, rootElm);
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.CLIENT_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.nodeToString(doc));
		}
		return doc;
	}
	
	private static void addElementData(Client client, String baseUrl, Element element) {
		String clientHref = baseUrl + Routes.CLIENT_ROUTE.replaceFirst("\\{id\\}", client.getId());

		element.setAttribute("id", client.getId());
		element.setAttribute("href", clientHref);
		element.setAttribute("secret", client.getSecret());
		element.setAttribute("role", client.getRole().toString());
		element.setAttribute("contact", client.getContactInfo());
		element.setAttribute("priority", client.getPriority().name().toLowerCase());
	}
}
