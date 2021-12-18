package org.daisy.pipeline.webservice.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.pipeline.webservice.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

public class AliveXmlWriter {

	private static Logger logger = LoggerFactory.getLogger(AliveXmlWriter.class.getName());
	private static String version = null;

	public Document getXmlDocument() {
		Document doc = XmlUtils.createDom("alive");
		Element aliveElm = doc.getDocumentElement();
		aliveElm.setAttribute("localfs", Boolean.valueOf(Properties.LOCALFS.get()) ? "true" : "false");
		aliveElm.setAttribute("authentication", Properties.AUTHENTICATION.get());
		aliveElm.setAttribute("version", getVersion());
		if (!XmlValidator.validate(doc, XmlValidator.ALIVE_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
			logger.error(XmlUtils.DOMToString(doc));
		}
		return doc;
	}

	private static String getVersion() {
		if (version == null) {
			String releaseDescriptorPath = Properties.RELEASE_DESCRIPTOR.get();
			if (releaseDescriptorPath != null) {
				File releaseDescriptor = new File(releaseDescriptorPath);
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
