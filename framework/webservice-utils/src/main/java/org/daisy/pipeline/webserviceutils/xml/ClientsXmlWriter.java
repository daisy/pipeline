package org.daisy.pipeline.webserviceutils.xml;

import java.util.List;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webserviceutils.Routes;
import org.daisy.pipeline.webserviceutils.xml.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClientsXmlWriter {
	
	private List<? extends Client> clients = null;
	private static Logger logger = LoggerFactory.getLogger(ClientsXmlWriter.class.getName());

	public ClientsXmlWriter(List<? extends Client> clients) {
		this.clients = clients;
	}
	
	public Document getXmlDocument() {
		if (clients == null) {
			logger.warn("Could not generate XML for null clients");
			return null;
		}
		return clientsToXmlDoc();
	}
	
	private Document clientsToXmlDoc() {
		String baseUri = new Routes().getBaseUri();
		Document doc = XmlUtils.createDom("clients");
		Element clientsElm = doc.getDocumentElement();
		clientsElm.setAttribute("href", baseUri + Routes.CLIENTS_ROUTE);
		for (Client client : clients) {
			ClientXmlWriter writer = XmlWriterFactory.createXmlWriterForClient(client);
			writer.addAsElementChild(clientsElm);
		}
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.CLIENTS_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}
		return doc;
	}
}