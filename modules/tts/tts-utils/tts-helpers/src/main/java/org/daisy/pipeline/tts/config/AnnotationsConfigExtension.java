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

	private Logger Logger = LoggerFactory.getLogger(AnnotationsConfigExtension.class);

	@Override
	public boolean parseNode(XdmNode node, URI documentURI) {
		String name = node.getNodeName().getLocalName();
		if ("annotations".equalsIgnoreCase(name)) {
			String href = node.getAttributeValue(new QName(null, "href"));
			String type = node.getAttributeValue(new QName(null, "type"));
			if (type == null) {
				Logger.debug("missing attribute @type in node <annotations> of the config file");
				return false;
			}
			if (!mAnnotations.containsKey(type))
				mAnnotations.put(type, new ArrayList<XdmNode>());
			if (href != null) {
				XdmNode external = ConfigReader.readFromURIinsideConfig(href, node
				        .getProcessor(), documentURI);
				if (external != null) {
					Logger.info("custom annotations read from " + external.getDocumentURI());
					mAnnotations.get(type).add(external);
				}
			} else {
				Logger.info("custom embedded annotations read from " + documentURI);
				mAnnotations.get(type).add(node);
			}
			return true;
		}
		return false;
	}

	public Collection<XdmNode> getAnnotations(String mediatype) {
		Collection<XdmNode> res = mAnnotations.get(mediatype);
		if (res != null)
			return res;
		return Collections.EMPTY_LIST;
	}

	@Override
	public void setParentReader(ConfigReader cr) {
	}

	private Map<String, List<XdmNode>> mAnnotations = new HashMap<String, List<XdmNode>>();
}
