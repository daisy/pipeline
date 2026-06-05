package org.daisy.pipeline.fileset;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.daisy.common.file.Resource;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;

/**
 * Java counterpart of {@code <d:fileset>} in XProc.
 */
public class Fileset {
	private Fileset() {}

	public static List<Resource> unmarshall(BaseURIAwareXMLStreamReader xml) throws XMLStreamException {
		List<Resource> fileset = new ArrayList<>();
		URI filesetBase = xml.getBaseURI();
		int depth = 0;
	  document: while (true) {
			try {
				int event = xml.next();
				switch (event) {
				case START_DOCUMENT:
					break;
				case END_DOCUMENT:
					break document;
				case START_ELEMENT:
					if (depth == 0 && XMLConstants.D_FILESET.equals(xml.getName())) {
						for (int i = 0; i < xml.getAttributeCount(); i++)
							if (XMLConstants.XML_BASE.equals(xml.getAttributeName(i))) {
								filesetBase = filesetBase.resolve(xml.getAttributeValue(i));
								break;
							}
						depth++;
						break;
					} else if (depth == 1 && XMLConstants.D_FILE.equals(xml.getName())) {
						URI href = null;
						URI originalHref = null;
						String mediaType = null;
						for (int i = 0; i < xml.getAttributeCount(); i++)
							if (XMLConstants._HREF.equals(xml.getAttributeName(i)))
								href = filesetBase.resolve(xml.getAttributeValue(i));
							else if (XMLConstants._ORIGINAL_HREF.equals(xml.getAttributeName(i)))
								originalHref = filesetBase.resolve(xml.getAttributeValue(i));
							else if (XMLConstants._MEDIA_TYPE.equals(xml.getAttributeName(i)))
								mediaType = xml.getAttributeValue(i);
						if (href != null) {
							if (originalHref != null)
								fileset.add(Resource.load(originalHref, href, mediaType));
							else
								fileset.add(Resource.load(href, mediaType));
						}
					}
					{ // consume whole element
						int d = depth + 1;
					  element: while (true) {
							event = xml.next();
							switch (event) {
							case START_ELEMENT:
								d++;
								break;
							case END_ELEMENT:
								d--;
								if (d == depth) break element;
							default:
							}
						}
					}
					break;
				case END_ELEMENT:
					depth--;
					break;
				default:
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
		return fileset;
	}

	/**
	 * @param xmlBase if not <code>null</code>, use this to create a <code>xml:base</code> attribute
	 *                and to relativize <code>href</code> attributes against. It is up to the caller
	 *                to set the base URI on the document (see {@link BaseURIAwareXMLStreamWriter}).
	 */
	public static void marshall(XMLStreamWriter xml, URI xmlBase, List<Resource> fileset) throws XMLStreamException {
		xml.writeStartDocument();
		writeStartElement(xml, XMLConstants.D_FILESET);
		if (xmlBase != null)
			writeAttribute(xml, XMLConstants.XML_BASE, xmlBase.toASCIIString());
		for (Resource file : fileset) {
			writeStartElement(xml, XMLConstants.D_FILE);
			writeAttribute(xml,
			               XMLConstants._HREF,
			               file.getPath(xmlBase).toASCIIString());
			if (file.getMediaType().isPresent())
				writeAttribute(xml, XMLConstants._MEDIA_TYPE, file.getMediaType().get());
			try {
				writeAttribute(xml,
				               XMLConstants._ORIGINAL_HREF,
				               file.readAsFile().toURI().toASCIIString());
			} catch (UnsupportedOperationException e) {
			}
			xml.writeEndElement();
		}
		xml.writeEndElement();
		xml.writeEndDocument();
	}

	public static final class XMLConstants {
		private XMLConstants() {}

		public static final QName D_FILESET = new QName("http://www.daisy.org/ns/pipeline/data", "fileset", "d");
		public static final QName XML_BASE = new QName(javax.xml.XMLConstants.XML_NS_URI, "base", "xml");
		public static final QName D_FILE = new QName("http://www.daisy.org/ns/pipeline/data", "file", "d");
		public static final QName _MEDIA_TYPE = new QName("media-type");
		public static final QName _HREF = new QName("href");
		public static final QName _ORIGINAL_HREF = new QName("original-href");
	}
}
