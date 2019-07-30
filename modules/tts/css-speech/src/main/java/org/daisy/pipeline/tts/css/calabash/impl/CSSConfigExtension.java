package org.daisy.pipeline.tts.css.calabash.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSSConfigExtension implements ConfigReader.Extension {

	private Logger Logger = LoggerFactory.getLogger(CSSConfigExtension.class);

	@Override
	public boolean parseNode(XdmNode node, URI documentURI) {
		String name = node.getNodeName().getLocalName();
		if ("css".equalsIgnoreCase(name)) {
			String href = node.getAttributeValue(new QName(null, "href"));
			if (href != null) {
				URL url = ConfigReader.URIinsideConfig(href, documentURI);
				if (url != null)
					try {
						mCSSuris.add(url.toURI());
					} catch (URISyntaxException e) {
					}
			} else {
				String content = node.getStringValue();
				if (content != null && !content.isEmpty()) {
					mEmbeddedCSS.add(content);
				}
			}
			return true;
		}
		return false;
	}

	public Collection<URI> getCSSstylesheetURIs() {
		return mCSSuris;
	}

	public Collection<String> getEmbeddedCSS() {
		return mEmbeddedCSS;
	}

	@Override
	public void setParentReader(ConfigReader cr) {
	}

	private Collection<URI> mCSSuris = new ArrayList<URI>();
	private Collection<String> mEmbeddedCSS = new ArrayList<String>();
}
