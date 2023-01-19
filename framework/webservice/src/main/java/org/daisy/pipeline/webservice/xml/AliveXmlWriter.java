package org.daisy.pipeline.webservice.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

public class AliveXmlWriter {

	private static Logger logger = LoggerFactory.getLogger(AliveXmlWriter.class.getName());
	private static String version = null;

	private final boolean isLocalFS;
	private final boolean isAuthenticationEnabled;

	public AliveXmlWriter(boolean isLocalFS, boolean isAuthenticationEnabled) {
		this.isLocalFS = isLocalFS;
		this.isAuthenticationEnabled = isAuthenticationEnabled;
	}

	public Document getXmlDocument() {
		Document doc = XmlUtils.createDom("alive");
		Element aliveElm = doc.getDocumentElement();
		aliveElm.setAttribute("localfs", isLocalFS ? "true" : "false");
		aliveElm.setAttribute("authentication", isAuthenticationEnabled ? "true" : "false");
		aliveElm.setAttribute("version", getVersion());
		if (!XmlValidator.validate(doc, XmlValidator.ALIVE_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.nodeToString(doc));
		}
		return doc;
	}

	private static String getVersion() {
		if (version == null) {
			String home = org.daisy.common.properties.Properties.getProperty("org.daisy.pipeline.home");
			if (home != null) {
				// pipeline-assembly is responsible for placing the file at this location
				File releaseDescriptor = new File(home + "/etc/releaseDescriptor.xml");
				if (releaseDescriptor.isFile()) {
					try {
						Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(releaseDescriptor);
						version = doc.getDocumentElement().getAttribute("version");
					} catch (ParserConfigurationException e) {
					} catch (SAXException e) {
					} catch (IOException e) {
					}
				}
			}
			if (version == null) version = "???";
		}
		return version;
	}
}
