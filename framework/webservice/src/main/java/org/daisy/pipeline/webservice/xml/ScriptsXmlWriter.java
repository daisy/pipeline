package org.daisy.pipeline.webservice.xml;

import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ScriptsXmlWriter {
	
	private final String baseUrl;
	Iterable<XProcScript> scripts = null;
	private static Logger logger = LoggerFactory.getLogger(ScriptsXmlWriter.class.getName());

	/**
	 * @param baseUrl Prefix to be included at the beginning of <code>href</code>
	 *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
	 *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
	 *                absolute paths relative to the domain name.
	 */
	public ScriptsXmlWriter(Iterable<XProcScript> scripts, String baseUrl) {
		this.scripts = scripts;
		this.baseUrl = baseUrl;
	}
	
	public Document getXmlDocument() {
		if (scripts == null) {
			logger.warn("Could not create XML for null scripts");
			return null;
		}
		return scriptsToXml(scripts);
	}
	
	private Document scriptsToXml(Iterable<XProcScript> scripts) {
		Document doc = XmlUtils.createDom("scripts");
		Element scriptsElm = doc.getDocumentElement();
		scriptsElm.setAttribute("href", baseUrl + Routes.SCRIPTS_ROUTE);
		
		for (XProcScript script : scripts) {
			ScriptXmlWriter writer = new ScriptXmlWriter(script, baseUrl);
			writer.addAsElementChild(scriptsElm);
		}
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.SCRIPTS_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}

		return doc;
	}	
}
