package org.daisy.braille.utils.pef;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

class StaxPEFBook {
	private static final Pattern EIGHT_DOT_PATTERN = Pattern.compile("[\u2840-\u28ff]");

	private static final String PEF_NS = "http://www.daisy.org/ns/2008/pef";
	private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
	private static final QName META_TAG = new QName(PEF_NS, "meta");
	private static final QName VOLUME_TAG = new QName(PEF_NS, "volume");
	private static final QName SECTION_TAG = new QName(PEF_NS, "section");
	private static final QName PAGE_TAG = new QName(PEF_NS, "page");
	private static final QName ROW_TAG = new QName(PEF_NS, "row");

	private static final QName ROWS_ATTR = new QName("rows");
	private static final QName COLS_ATTR = new QName("cols");
	private static final QName DUPLEX_ATTR = new QName("duplex");

	private final XMLInputFactory inFactory;
	private XMLEventReader reader;

	private int volumes;
	private int sectionNumber;
	private int pages;
	private int pageTags;
	private HashMap<String, List<String>> metadata;
	private Map<SectionIdentifier, Integer> started;
	private List<Integer> sectionsInVolume;
	private int maxWidth;
	private int maxHeight;
	private boolean containsEightDot;
	private boolean compatibilityMode;

	private XMLEvent event;
	boolean evenLast = false;

	private StaxPEFBook() {
		inFactory = XMLInputFactory.newInstance();
		inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);        
		inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
		inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
		inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
	}

	static PEFBook loadStax(URI uri) {
		return loadStax(uri, false);
	}

	static PEFBook loadStax(URI uri, boolean compatibilityMode) {
		StaxPEFBook spb = new StaxPEFBook();
		try {
			spb.compatibilityMode = compatibilityMode;
			return spb.parse(uri);
		} catch (XMLStreamException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private PEFBook parse(URI uri) throws XMLStreamException, IOException {
		String encoding = null;
		volumes = 0;
		pages = 0;
		pageTags = 0;
		metadata = new HashMap<>();
		started = new HashMap<>();
		sectionsInVolume = new ArrayList<>();
		maxWidth = 0;
		maxHeight = 0;
		containsEightDot = false;
		try (InputStream is = uri.toURL().openStream()) {
			reader = inFactory.createXMLEventReader(is);

			while (reader.hasNext()) {
				event = reader.nextEvent();
				if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
					if (VOLUME_TAG.equals(event.asStartElement().getName())) {
						scanVolume();
					} else if (META_TAG.equals(event.asStartElement().getName())) {
						scanMeta();
					}
				} else if (event.getEventType()==XMLStreamConstants.START_DOCUMENT) {
					StartDocument sd = (StartDocument)event;
					if (sd.encodingSet()) {
						encoding = sd.getCharacterEncodingScheme();
					}
				}
			}
		}
		if (evenLast) {
			pages--;
		}		

		// This is done because toArray cannot cast Integer to int
		int[] sectionsInVolumeArray = new int[sectionsInVolume.size()];
		for (int i=0; i<sectionsInVolume.size(); i++) {
			sectionsInVolumeArray[i] = sectionsInVolume.get(i);
		}

		return new PEFBook(uri, metadata, volumes, pages, pageTags, maxWidth, maxHeight, encoding, containsEightDot, started, sectionsInVolumeArray);
	}

	private void scanMeta() throws XMLStreamException {
		if (!(event.getEventType()==XMLStreamConstants.START_ELEMENT && META_TAG.equals(event.asStartElement().getName()))) {
			throw new XMLStreamException("Parse error.");
		}
		int level = 1;
		List<String> al;
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				level++;
				QName start = event.asStartElement().getName();
				if (event.asStartElement().getName().getNamespaceURI().equals(DC_NS)) {
					String s = "";
					while (reader.hasNext()) {
						event = reader.nextEvent();
						if (event.getEventType()==XMLStreamConstants.CHARACTERS) {
							s += event.asCharacters().getData();
						} else if (event.getEventType()==XMLStreamConstants.END_ELEMENT) {
							if (event.asEndElement().getName().equals(start)) {
								level--;
								break;
							}
						}
					}
					//set
					String name = start.getLocalPart();
					if (metadata.containsKey(name)) {
						al = metadata.remove(name);
					} else {
						al = new ArrayList<>();
					}
					al.add(s);
					metadata.put(name, al);
				}
			} else if (event.getEventType()==XMLStreamConstants.END_ELEMENT) {
				level--;
				if (level<=0) {
					break;
				}
			}
		}
	}

	private void scanVolume() throws XMLStreamException {
		if (!eventIsStartElement(VOLUME_TAG)) {
			throw new XMLStreamException("Parse error.");
		}
		volumes++;
		sectionNumber = 0;
		SectionAttributes v = new SectionAttributes(parseIntAttribute(event.asStartElement(), ROWS_ATTR, 0), 
				parseIntAttribute(event.asStartElement(), COLS_ATTR, 0),
				parseBooleanAttribute(event.asStartElement(), DUPLEX_ATTR, false));
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				if (SECTION_TAG.equals(event.asStartElement().getName())) {
					scanSection(v);
				}
			} else if (eventIsEndElement(VOLUME_TAG)) {
				break;
			}
		}
		sectionsInVolume.add(compatibilityMode?1:sectionNumber);
	}

	private void scanSection(SectionAttributes v) throws XMLStreamException {
		if (!eventIsStartElement(SECTION_TAG)) {
			throw new XMLStreamException("Parse error.");
		}
		sectionNumber++;
		//in compatibility mode, only store section nr 1
		if (!compatibilityMode || sectionNumber==1) {
			started.put(new SectionIdentifier(volumes, sectionNumber), pages+1);
		}
		SectionAttributes s = new SectionAttributes(parseIntAttribute(event.asStartElement(), ROWS_ATTR, v.rows), 
				parseIntAttribute(event.asStartElement(), COLS_ATTR, v.cols),
				parseBooleanAttribute(event.asStartElement(), DUPLEX_ATTR, v.duplex));

		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				if (PAGE_TAG.equals(event.asStartElement().getName())) {
					pageTags++;
					pages += (s.duplex?1:2);
					scanPage();
				}
			} else if (eventIsEndElement(SECTION_TAG)) {
				//two operations in one, stack.pop() must be performed
				if (s.duplex && pages % 2 == 1) {
					pages++;
					evenLast = true;
				} else {
					evenLast = false;
				}
				break;
			}
		}
	}

	private void scanPage() throws XMLStreamException {
		if (!eventIsStartElement(PAGE_TAG)) {
			throw new XMLStreamException("Parse error.");
		}
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				if (ROW_TAG.equals(event.asStartElement().getName())) {
					scanRow();
				}
			} else if (eventIsEndElement(PAGE_TAG)) {
				break;
			}
		}
	}

	private void scanRow() throws XMLStreamException {
		if (!eventIsStartElement(ROW_TAG)) {
			throw new XMLStreamException("Parse error.");
		}
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.CHARACTERS) {
				if (EIGHT_DOT_PATTERN.matcher(event.asCharacters().getData()).find()) {
					containsEightDot = true;
				}
			} else if (eventIsEndElement(ROW_TAG)) {
				break;
			}
		}
	}

	private boolean eventIsStartElement(QName name) {
		return event.getEventType()==XMLStreamConstants.START_ELEMENT && name.equals(event.asStartElement().getName());
	}

	private boolean eventIsEndElement(QName name) {
		return  event.getEventType()==XMLStreamConstants.END_ELEMENT && name.equals(event.asEndElement().getName());
	}

	private int parseIntAttribute(StartElement element, QName att, int def) {
		Attribute a = element.getAttributeByName(att);
		if (a!=null) {
			try {
				return Integer.parseInt(a.getValue());
			} catch (NumberFormatException e) { }
		}
		return def;
	}

	private boolean parseBooleanAttribute(StartElement element, QName att, boolean def) {
		Attribute a = element.getAttributeByName(att);
		if (a!=null) {
			try {
				return Boolean.parseBoolean(a.getValue());
			} catch (NumberFormatException e) { }
		}
		return def;
	}

	private final class SectionAttributes {
		private final int rows;
		private final int cols;
		private final boolean duplex;

		public SectionAttributes(int rows, int cols, boolean duplex) {
			this.rows = rows;
			this.cols = cols;
			this.duplex = duplex;
			maxWidth = Math.max(maxWidth, this.cols);
			maxHeight = Math.max(maxHeight, this.rows);
		}
	}

}
