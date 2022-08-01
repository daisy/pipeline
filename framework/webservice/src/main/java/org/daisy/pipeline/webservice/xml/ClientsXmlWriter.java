package org.daisy.pipeline.webservice.xml;

import java.util.List;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClientsXmlWriter {
	
	private final String baseUrl;
	private List<? extends Client> clients = null;
	private static Logger logger = LoggerFactory.getLogger(ClientsXmlWriter.class.getName());

	/**
	 * @param baseUrl Prefix to be included at the beginning of <code>href</code>
	 *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
	 *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
	 *                absolute paths relative to the domain name.
	 */
	public ClientsXmlWriter(List<? extends Client> clients, String baseUrl) {
		this.clients = clients;
		this.baseUrl = baseUrl;
	}
	
	public Document getXmlDocument() {
		if (clients == null) {
			logger.warn("Could not generate XML for null clients");
			return null;
		}
		return clientsToXmlDoc();
	}
	
	private Document clientsToXmlDoc() {
		Document doc = XmlUtils.createDom("clients");
		Element clientsElm = doc.getDocumentElement();
		clientsElm.setAttribute("href", baseUrl + Routes.CLIENTS_ROUTE);
		for (Client client : clients) {
			ClientXmlWriter writer = new ClientXmlWriter(client, baseUrl);
			writer.addAsElementChild(clientsElm);
		}
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.CLIENTS_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}
		return doc;
	}
}
