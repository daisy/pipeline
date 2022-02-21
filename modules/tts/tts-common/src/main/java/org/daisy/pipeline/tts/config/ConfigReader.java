package org.daisy.pipeline.tts.config;

import java.io.File;
import java.net.URI;
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

import org.daisy.common.file.URLs;
import org.daisy.common.properties.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class ConfigReader implements ConfigProperties {

	private static Logger Logger = LoggerFactory.getLogger(ConfigReader.class);

	public static final String HostProtectionProperty = "org.daisy.pipeline.tts.host.protection";
	private static final String ttsConfigProperty = "org.daisy.pipeline.tts.config";

	public interface Extension {
		/**
		 * @param node has a non-null local name
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
		String staticConfigPath = Properties.getProperty(ttsConfigProperty);
		if (staticConfigPath != null) {
			XdmNode content = readFromURIinsideConfig(staticConfigPath, saxonproc, URLs.asURI(new File("./")));
			if (content != null)
				readConfig(null, content, extensions);
		}
		if (doc != null) {
			readConfig(mDynamicProps, doc, extensions);
		}
		for (String key : Properties.propertyNames()) {
			if (key.startsWith("org.daisy.pipeline.tts.")
			    && !key.equals(HostProtectionProperty)
			    && !key.equals(ttsConfigProperty)) {
				String value = Properties.getProperty(key);
				mStaticProps.put(key, value);
			}
		}
		mAllProps = new HashMap<String, String>();
		mAllProps.putAll(mStaticProps);
		if (Properties.getProperty(HostProtectionProperty, "true").equalsIgnoreCase("false"))
			mAllProps.putAll(mDynamicProps);
	}

	static public URL URIinsideConfig(String pathOrURI, URI relativeTo) {
		URI uri = null;
		
		// absolute path check
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")){
			// on windows machine, test for each drive, 
			for(File root : File.listRoots()){
				String rootDrive = root.getAbsolutePath().split(":")[0] + ":";
				if (pathOrURI.startsWith(rootDrive)) {
					uri = URLs.asURI(new File(pathOrURI));
					break;
				} else if (pathOrURI.startsWith("/") && System.getenv("SYSTEMDRIVE").equals(rootDrive)){
					uri = URLs.asURI(new File(rootDrive + pathOrURI));
				}
			}
		} else if (pathOrURI.startsWith("/")) {
			// test for unix machine
			uri = URLs.asURI(new File(pathOrURI));
		} 
		
		
		if(uri == null) {
			try {
				uri = URLs.asURI(pathOrURI);
			} catch (Exception e) {
				Logger.debug("URI " + uri + ": wrong format " + e);
				return null;
			}
		}
		if (!uri.isAbsolute()) {
			if (relativeTo == null) {
				Logger.debug("URI " + uri + " must be absolute");
				return null;
			}
			uri = relativeTo.resolve(uri);
		}
		try {
			return URLs.asURL(uri);
		} catch (Exception e) {
			Logger.debug("Malformed URL: " + uri);
			return null;
		}
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
				if ("property".equalsIgnoreCase(qname.getLocalName())) {
					String key = node.getAttributeValue(new QName(null, "key"));
					String value = node.getAttributeValue(new QName(null, "value"));
					if (key == null || value == null) {
						Logger.warn("Missing key or value for config's property "
						        + node.toString());
					} else if (props != null) {
						if (!key.startsWith("org.daisy.pipeline.tts."))
							// for backwards compatibility
							key = "org.daisy.pipeline.tts." + key;
						props.put(key, value);
					} else {
						Logger.warn("Ignoring property " + node.toString()
						        + " inside static TTS config file.\nPlease use a"
						        + " system property or environment variable instead.");
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
