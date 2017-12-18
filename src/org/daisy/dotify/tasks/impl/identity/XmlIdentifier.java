package org.daisy.dotify.tasks.impl.identity;

import org.daisy.dotify.api.identity.IdentificationFailedException;
import org.daisy.dotify.api.identity.Identifier;
import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.common.xml.XMLInfo;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;

/**
 * Provides an identifier for xml files. This identifier will attach some additional
 * information about the root element of the file. These can be accessed using 
 * {@link AnnotatedFile#getProperties()}. The keys are included below.
 * 
 * In addition, the meta data regarding some xml formats that are relevant to this
 * bundle (namely dtbook, html, obfl and pef) are specified to a greater detail
 * with respect to format name and media type.
 * 
 * @author Joel Håkansson
 */
public class XmlIdentifier implements Identifier {
	/**
	 * Defines the property key for the root element namespace.
	 */
	public static final String XMLNS_KEY = "xmlns";
	/**
	 * Defines the property key for the root element local name.
	 */
	public static final String LOCAL_NAME_KEY = "local-name";
	/**
	 * Defines the property key for the root element attributes.
	 */
	public static final String ATTRIBUTES_KEY = "attributes";

	/**
	 * Creates a new xml identifier instance.
	 */
	public XmlIdentifier() {
		super();
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
