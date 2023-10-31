package org.daisy.pipeline.tts.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.tts.config.ConfigReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicPropertiesExtension implements ConfigReader.Extension {

	private static final Logger logger = LoggerFactory.getLogger(VoiceConfigExtension.class);
	private static final String HOST_PROTECTION_PROPERTY = Properties.getProperty("org.daisy.pipeline.tts.host.protection", "true");

	private Map<String,String> props = new HashMap<>();

	@Override
	public boolean parseNode(XdmNode node, URI documentURI, ConfigReader parent) {
		String name = node.getNodeName().getLocalName();
		String key = node.getAttributeValue(new QName(null, "key"));
		if ("property".equalsIgnoreCase(name)) {
			if (HOST_PROTECTION_PROPERTY.equalsIgnoreCase("false")) {
				//	logger.warn("'" + HOST_PROTECTION_PROPERTY + "' setting is deprecated. " +
				//	            "It may become unavailable in future version of DAISY Pipeline.");
				if (key == null) {
					logger.warn("Missing key for config's property " + node.toString());
				} else {
					String value = node.getAttributeValue(new QName(null, "value"));
					if (value == null) {
						logger.warn("Missing value for config's property " + node.toString());
					} else {
						if (!key.startsWith("org.daisy.pipeline.tts."))
							key = "org.daisy.pipeline.tts." + key;
						props.put(key, value);
					}
				}
			} else {
				if (key != null) {
					logger.warn("Ignoring property " + key
					            + " inside TTS config file.\nPlease use a"
					            + " system property or environment variable instead.");
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public Map<String,String> getDynamicProperties() {
		return props;
	}
}
