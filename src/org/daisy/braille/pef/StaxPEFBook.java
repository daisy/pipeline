package org.daisy.braille.pef;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private static final Pattern eightDotPattern = Pattern.compile("[\u2840-\u28ff]");
	
	private final static String pefns = "http://www.daisy.org/ns/2008/pef";
	private final static String dcns = "http://purl.org/dc/elements/1.1/";
	private final static QName meta = new QName(pefns, "meta");
	private final static QName volume = new QName(pefns, "volume");
	private final static QName section = new QName(pefns, "section");
	private final static QName page = new QName(pefns, "page");
	private final static QName row = new QName(pefns, "row");
	
	private final static QName rowsqn = new QName("rows");
	private final static QName colsqn = new QName("cols");
	private final static QName duplexqn = new QName("duplex");
	
	private final XMLInputFactory inFactory;
	private XMLEventReader reader;
	
	private String encoding;
	private int volumes;
	private int pages;
	private int pageTags;
	private HashMap<String, List<String>> metadata;
	private List<Integer> started;
	private int maxWidth;
	private int maxHeight;
	private boolean containsEightDot;
	
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
		StaxPEFBook spb = new StaxPEFBook();
		try {
			return spb.parse(uri);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private PEFBook parse(URI uri) throws MalformedURLException, XMLStreamException, IOException {
		encoding = null;
		volumes = 0;
		pages = 0;
		pageTags = 0;
		metadata = new HashMap<String, List<String>>();
		started = new ArrayList<Integer>();
		maxWidth = 0;
		maxHeight = 0;
		containsEightDot = false;
		reader = inFactory.createXMLEventReader(uri.toURL().openStream());
		
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				if (volume.equals(event.asStartElement().getName())) {
					scanVolume();
				} else if (meta.equals(event.asStartElement().getName())) {
					scanMeta();
				}
			} else if (event.getEventType()==XMLStreamConstants.START_DOCUMENT) {
				StartDocument sd = (StartDocument)event;
            	if (sd.encodingSet()) {
            	    encoding = sd.getCharacterEncodingScheme();
            	}
			}
		}
		if (evenLast) {
			pages--;
		}		

		int[] str = new int[started.size()];
		for (int i=0; i<started.size(); i++) {
			str[i] = started.get(i);
		}
		return new PEFBook(metadata, volumes, pages, pageTags, maxWidth, maxHeight, encoding, containsEightDot, str);
	}
	
	private void scanMeta() throws XMLStreamException {
		if (!(event.getEventType()==XMLStreamConstants.START_ELEMENT && meta.equals(event.asStartElement().getName()))) {
			throw new XMLStreamException("Parse error.");
		}
		int level = 1;
		List<String> al;
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				level++;
				QName start = event.asStartElement().getName();
				if (event.asStartElement().getName().getNamespaceURI().equals(dcns)) {
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
						al = new ArrayList<String>();
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
		if (!eventIsStartElement(volume)) {
			throw new XMLStreamException("Parse error.");
		}
		volumes++;
		started.add(pages+1);
		SectionAttributes v = new SectionAttributes(parseIntAttribute(event.asStartElement(), rowsqn, 0), 
								parseIntAttribute(event.asStartElement(), colsqn, 0),
								parseBooleanAttribute(event.asStartElement(), duplexqn, false));
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				if (section.equals(event.asStartElement().getName())) {
					scanSection(v);
				}
			} else if (eventIsEndElement(volume)) {
				break;
			}
		}
	}
	
	private void scanSection(SectionAttributes v) throws XMLStreamException {
		if (!eventIsStartElement(section)) {
			throw new XMLStreamException("Parse error.");
		}
		SectionAttributes s = new SectionAttributes(parseIntAttribute(event.asStartElement(), rowsqn, v.rows), 
				parseIntAttribute(event.asStartElement(), colsqn, v.cols),
				parseBooleanAttribute(event.asStartElement(), duplexqn, v.duplex));

		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				if (page.equals(event.asStartElement().getName())) {
					pageTags++;
					pages += (s.duplex?1:2);
					scanPage();
				}
			} else if (eventIsEndElement(section)) {
				//two operations in one, stack.pop() must be performed
				if (s.duplex==true && pages % 2 == 1) {
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
		if (!eventIsStartElement(page)) {
			throw new XMLStreamException("Parse error.");
		}
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT) {
				if (row.equals(event.asStartElement().getName())) {
					scanRow();
				}
			} else if (eventIsEndElement(page)) {
				break;
			}
		}
	}
	
	
	/*
	 * count(//pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='false']]/descendant::pef:page)*2 +
	 * Count all pages in non-duplex sections as 2 pages
	 * 
	 * count(//pef:section[ancestor-or-self::pef:*[@duplex][1][@duplex='true']]/descendant::pef:page) + 
	 * Count all pages in duplex sections as 1 page
	 * 
	 * count(//pef:section[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])
	 * Count all sections where duplex is true and page count is uneven...
	 * 
	 * -count(((//pef:section)[last()])[count(descendant::pef:page) mod 2 = 1][ancestor-or-self::pef:*[@duplex][1][@duplex='true']])
	 * except the last one
	 */
	private void scanRow() throws XMLStreamException {
		if (!eventIsStartElement(row)) {
			throw new XMLStreamException("Parse error.");
		}
		while (reader.hasNext()) {
			event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.CHARACTERS) {
				if (eightDotPattern.matcher(event.asCharacters().getData()).find()) {
					containsEightDot = true;
				}
			} else if (eventIsEndElement(row)) {
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

	private class SectionAttributes {
		private final int rows, cols;
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
