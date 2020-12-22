package org.daisy.pipeline.webservice.xml;

import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.webservice.Routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ScriptsXmlWriter {
	
	Iterable<XProcScript> scripts = null;
	private static Logger logger = LoggerFactory.getLogger(ScriptsXmlWriter.class.getName());
	
	public ScriptsXmlWriter(Iterable<XProcScript> scripts) {
		this.scripts = scripts;
	}
	
	public Document getXmlDocument() {
		if (scripts == null) {
			logger.warn("Could not create XML for null scripts");
			return null;
		}
		return scriptsToXml(scripts);
	}
	
	private Document scriptsToXml(Iterable<XProcScript> scripts) {
		String baseUri = new Routes().getBaseUri();
		Document doc = XmlUtils.createDom("scripts");
		Element scriptsElm = doc.getDocumentElement();
		scriptsElm.setAttribute("href", baseUri + Routes.SCRIPTS_ROUTE);
		
		for (XProcScript script : scripts) {
			ScriptXmlWriter writer = XmlWriterFactory.createXmlWriterForScript(script);
			writer.addAsElementChild(scriptsElm);
		}
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.SCRIPTS_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}

		return doc;
	}	
}
