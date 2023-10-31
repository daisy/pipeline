package org.daisy.pipeline.tts.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LexiconsConfigExtension implements ConfigReader.Extension {

	private final static Logger logger = LoggerFactory.getLogger(LexiconsConfigExtension.class);

	private List<XdmNode> lexicons = new ArrayList<>();

	@Override
	public boolean parseNode(XdmNode node, URI documentURI, ConfigReader parent) {
		String name = node.getNodeName().getLocalName();
		if ("lexicon".equalsIgnoreCase(name)) {
			String href = node.getAttributeValue(new QName(null, "href"));
			if (href != null) {
				XdmNode external = parent.parseXML(href, documentURI);
				if (external != null) {
					logger.info("custom annotations read from " + external.getBaseURI());
					lexicons.add(external);
				}
			} else {
				logger.info("custom embedded annotations read from " + documentURI);
				lexicons.add(node);
			}
			return true;
		}
		return false;
	}

	public Collection<XdmNode> getLexicons() {
		return lexicons;
	}
}
