package org.daisy.pipeline.webserviceutils.xml;

import org.daisy.pipeline.webserviceutils.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AliveXmlWriter {
	private static Logger logger = LoggerFactory.getLogger(AliveXmlWriter.class.getName());
	public Document getXmlDocument() {
		Document doc = XmlUtils.createDom("alive");
		Element aliveElm = doc.getDocumentElement();
		aliveElm.setAttribute("localfs", Boolean.valueOf(Properties.LOCALFS.get()) ? "true" : "false");
		aliveElm.setAttribute("authentication", Properties.AUTHENTICATION.get());
		aliveElm.setAttribute("version", Properties.FRAMEWORK_VERSION.get());
		if (!XmlValidator.validate(doc, XmlValidator.ALIVE_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
			logger.error(XmlUtils.DOMToString(doc));
		}
		return doc;
	}

}
