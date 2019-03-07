package org.daisy.pipeline.tts.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LexiconsConfigExtension implements ConfigReader.Extension {

	private Logger Logger = LoggerFactory.getLogger(LexiconsConfigExtension.class);
	private final Processor saxonproc;
	
	public LexiconsConfigExtension(Processor saxonproc) {
		this.saxonproc = saxonproc;
	}

	@Override
	public boolean parseNode(XdmNode node, URI documentURI) {
		String name = node.getNodeName().getLocalName();
		if ("lexicon".equalsIgnoreCase(name)) {
			String href = node.getAttributeValue(new QName(null, "href"));
			if (href != null) {
				XdmNode external = ConfigReader.readFromURIinsideConfig(href, saxonproc, documentURI);
				if (external != null) {
					Logger.info("custom annotations read from " + external.getBaseURI());
					mLexicons.add(external);
				}
			} else {
				Logger.info("custom embedded annotations read from " + documentURI);
				mLexicons.add(node);
			}
			return true;
		}
		return false;
	}

	public Collection<XdmNode> getLexicons() {
		return mLexicons;
	}

	@Override
	public void setParentReader(ConfigReader cr) {
	}

	private List<XdmNode> mLexicons = new ArrayList<XdmNode>();
}
