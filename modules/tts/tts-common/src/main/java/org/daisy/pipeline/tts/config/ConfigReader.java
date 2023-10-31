package org.daisy.pipeline.tts.config;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.common.file.URLs;
import org.daisy.common.properties.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;

public class ConfigReader {

	private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
	private static final String staticConfigPath = Properties.getProperty("org.daisy.pipeline.tts.config");

	public interface Extension {
		/**
		 * @param node has a non-null local name
		 * @return false is the parsing can keep going with other extensions
		 *         (most likely if @node is not related to the extension)
		 */
		public boolean parseNode(XdmNode node, URI documentURI, ConfigReader parent);
	}

	private final Processor saxonproc;

	public ConfigReader(Processor saxonproc, Extension... extensions) {
		this(saxonproc, null, extensions);
	}

	public ConfigReader(Processor saxonproc, XdmNode doc, Extension... extensions) {
		this.saxonproc = saxonproc;
		if (staticConfigPath != null) {
			XdmNode content = parseXML(staticConfigPath);
			if (content != null)
				read(content, extensions);
		}
		if (doc != null) {
			read(doc, extensions);
		}
	}

	/**
	 * Read document from URL within TTS configuration document.
	 *
	 * @param url  The URL to read from. If relative, it is resolved
	 *             against the base URI of the configuration document.
	 * @param base The base URI of the configuration document.
	 */
	protected XdmNode parseXML(String url, URI base) {
		try {
			return parseXML(resolve(url, base), saxonproc);
		} catch (Exception e) {
			logger.debug("Malformed URL: " + url);
			return null;
		}
	}

	// package private for unit tests
	static URL resolve(String url, URI base) {
		return URLs.asURL(URLs.resolve(base, URLs.asURI(url)));
	}

	/**
	 * Read document from URL.
	 *
	 * @param url The URL to read from. If relative, it is resolved
	 *            against the current directory.
	 */
	protected XdmNode parseXML(String url) {
		return parseXML(url, URLs.asURI(new File("./")));
	}

	/**
	 * Read document from URL.
	 *
	 * @param url The absolute URL to read from.
	 */
	private static XdmNode parseXML(URL url, Processor saxonproc) {
		if (url == null)
			return null;
		try {
			SAXSource source = new SAXSource(new InputSource(url.openStream()));
			source.setSystemId(url.toString());
			return saxonproc.newDocumentBuilder().build(source);
		} catch (Exception e) {
			logger.debug("error while reading " + url + ": " + e);
			return null;
		}
	}

	private void read(XdmNode doc, Extension... extensions) {
		URI docURI = doc.getBaseURI();
		XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
		XdmNode root = doc;
		while (doc.getNodeKind() != XdmNodeKind.ELEMENT && it.hasNext())
			root = (XdmNode) it.next();
		it = root.axisIterator(Axis.CHILD);
		while (it.hasNext()) {
			XdmNode node = (XdmNode) it.next();
			QName qname = node.getNodeName();
			if (qname != null) {
				boolean parsed = false;
				for (int k = 0; !parsed && k < extensions.length; ++k) {
					if ("css".equalsIgnoreCase(qname.getLocalName())) {
						logger.warn("Ignoring 'css' element " + node.toString()
						            + " inside TTS config file.\nPlease specify CSS style sheets"
						            + " through the designated option or attach them to the input"
						            + " document.");
					} else {
						parsed = extensions[k].parseNode(node, docURI, this);
					}
				}
			}
		}
	}
}
