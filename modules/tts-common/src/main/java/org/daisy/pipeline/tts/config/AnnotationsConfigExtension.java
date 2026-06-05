package org.daisy.pipeline.tts.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationsConfigExtension implements ConfigReader.Extension {

	private static final Logger logger = LoggerFactory.getLogger(AnnotationsConfigExtension.class);

	private final Map<String,List<XdmNode>> annotations = new HashMap<>();

	@Override
	public boolean parseNode(XdmNode node, URI documentURI, ConfigReader parent) {
		String name = node.getNodeName().getLocalName();
		if ("annotations".equalsIgnoreCase(name)) {
			String href = node.getAttributeValue(new QName(null, "href"));
			String type = node.getAttributeValue(new QName(null, "type"));
			if (type == null) {
				logger.debug("missing attribute @type in node <annotations> of the config file");
				return false;
			}
			if (!annotations.containsKey(type))
				annotations.put(type, new ArrayList<XdmNode>());
			if (href != null) {
				XdmNode external = parent.parseXML(href, documentURI);
				if (external != null) {
					logger.info("custom annotations read from " + external.getBaseURI());
					annotations.get(type).add(external);
				}
			} else {
				logger.info("custom embedded annotations read from " + documentURI);
				annotations.get(type).add(node);
			}
			return true;
		}
		return false;
	}

	public Collection<XdmNode> getAnnotations(String mediatype) {
		Collection<XdmNode> res = annotations.get(mediatype);
		if (res != null)
			return res;
		return Collections.EMPTY_LIST;
	}
}
