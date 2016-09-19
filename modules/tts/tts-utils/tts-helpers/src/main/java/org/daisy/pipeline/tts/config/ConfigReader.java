package org.daisy.pipeline.tts.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class ConfigReader implements ConfigProperties {

	private static Logger Logger = LoggerFactory.getLogger(ConfigReader.class);

	public static String HostProtectionProperty = "host.protection";

	public interface Extension {
		/**
		 * @node has a non-null local name
		 * @return false is the parsing can keep going with other extensions
		 *         (most likely if @node is not related to the extension)
		 */
		public boolean parseNode(XdmNode node, URI documentURI);

		public void setParentReader(ConfigReader cr);
	}

	public ConfigReader(Processor saxonproc, Extension... extensions) {
		this(saxonproc, null, extensions);
	}

	public ConfigReader(Processor saxonproc, XdmNode doc, Extension... extensions) {
		String staticConfigPath = System.getProperty("tts.config");
		if (staticConfigPath != null) {
			XdmNode content = readFromURIinsideConfig(staticConfigPath, saxonproc, null);
			if (content != null)
				readConfig(mStaticProps, content, extensions);
		}
		if (doc != null) {
			readConfig(mDynamicProps, doc, extensions);
		}
		mAllProps = new HashMap<String, String>();
		mAllProps.putAll(mStaticProps);
		if (System.getProperty(HostProtectionProperty, "true").equalsIgnoreCase("false"))
			mAllProps.putAll(mDynamicProps);
	}

	static public URL URIinsideConfig(String pathOrURI, URI relativeTo) {
		if (pathOrURI.startsWith("/")) {
			pathOrURI = "file://" + pathOrURI;
		}
		URI uri = null;
		try {
			uri = new URI(pathOrURI);
		} catch (URISyntaxException e) {
			Logger.debug("URI " + uri + ": wrong format " + e);
			return null;
		}
		if (!uri.isAbsolute()) {
			if (relativeTo == null) {
				Logger.debug("URI " + uri + " must be absolute");
				return null;
			}
			uri = relativeTo.resolve(uri);
		}

		URL url = null;
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			Logger.debug("Malformed URL: " + uri);
		}
		return url;
	}

	static public XdmNode readFromURIinsideConfig(String pathOrURI, Processor saxonproc,
	        URI relativeTo) {
		URL url = URIinsideConfig(pathOrURI, relativeTo);
		if (url != null) {
			try {
				SAXSource source = new SAXSource(new InputSource(url.openStream()));
				source.setSystemId(url.toString());
				return saxonproc.newDocumentBuilder().build(source);
			} catch (Exception e) {
				Logger.debug("error while reading " + url + ": " + e);
			}
		}
		return null;
	}

	private void readConfig(Map<String, String> props, XdmNode doc, Extension... extensions) {

		for (Extension ext : extensions) {
			ext.setParentReader(this);
		}

		URI docURI = doc.getDocumentURI();

		XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
		XdmNode root = doc;
		while (doc.getNodeKind() != XdmNodeKind.ELEMENT && it.hasNext())
			root = (XdmNode) it.next();

		it = root.axisIterator(Axis.CHILD);
		while (it.hasNext()) {
			XdmNode node = (XdmNode) it.next();
			QName qname = node.getNodeName();
			if (qname != null) {
				if ("property".equalsIgnoreCase(qname.getLocalName())) {
					String key = node.getAttributeValue(new QName(null, "key"));
					String value = node.getAttributeValue(new QName(null, "value"));
					if (key == null || value == null) {
						Logger.warn("Missing key or value for config's property "
						        + node.toString());
					} else {
						props.put(key, value);
					}
				} else {
					boolean parsed = false;
					for (int k = 0; !parsed && k < extensions.length; ++k) {
						parsed = extensions[k].parseNode(node, docURI);
					}
				}
			}
		}
	}

	@Override
	public Map<String, String> getDynamicProperties() {
		return mDynamicProps;
	}

	@Override
	public Map<String, String> getStaticProperties() {
		return mStaticProps;
	}

	@Override
	public Map<String, String> getAllProperties() {
		return mAllProps;
	}

	private Map<String, String> mStaticProps = new HashMap<String, String>();
	private Map<String, String> mDynamicProps = new HashMap<String, String>();
	private Map<String, String> mAllProps;
}
