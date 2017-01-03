package org.daisy.dotify.impl.identity;

import org.daisy.dotify.api.identity.IdentificationFailedException;
import org.daisy.dotify.api.identity.Identifier;
import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.common.xml.XMLInfo;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;

public class XmlIdentifier implements Identifier {
	public static String XMLNS_KEY = "xmlns";
	public static String LOCAL_NAME_KEY = "local-name";
	public static String ATTRIBUTES_KEY = "attributes";

	public XmlIdentifier() {
		// no fields
	}

	@Override
	public AnnotatedFile identify(AnnotatedFile f) throws IdentificationFailedException {
		XMLInfo info = null;
		try {
			info = XMLTools.parseXML(f.getFile());
		} catch (XMLToolsException e) {
			throw new IdentificationFailedException(e);
		}
		if (info==null) {
			throw new IdentificationFailedException("Not well-formed XML: " + f.getFile());
		} else {
			DefaultAnnotatedFile.Builder ret = new DefaultAnnotatedFile.Builder(f.getFile())
					.property(XMLNS_KEY, info.getUri())
					.property(LOCAL_NAME_KEY, info.getLocalName())
					.property(ATTRIBUTES_KEY, info.getAttributes());
			if ("http://www.daisy.org/z3986/2005/dtbook/".equals(info.getUri())) {
				ret.formatName("dtbook").extension("xml").mediaType("application/x-dtbook+xml");
			} else if ("http://www.w3.org/1999/xhtml".equals(info.getUri())) {
				ret.formatName("html").extension("html").mediaType("application/xhtml+xml");
			} else if ("http://www.daisy.org/ns/2011/obfl".equals(info.getUri())) {
				ret.formatName("obfl").extension("obfl").mediaType("application/x-obfl+xml");
			} else if ("http://www.daisy.org/ns/2008/pef".equals(info.getUri())) {
				ret.formatName("pef").extension("pef").mediaType("application/x-pef+xml");
			} else {
				ret.formatName("xml").extension("xml").mediaType("application/xml");
			}
			return ret.build();
		}
	}

}
