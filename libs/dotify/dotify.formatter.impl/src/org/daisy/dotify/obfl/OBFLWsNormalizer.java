package org.daisy.dotify.obfl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.XMLEvent;

public class OBFLWsNormalizer extends XMLParserBase {
	private final XMLEventReader input;
	private final OutputStream out;
	private final XMLEventFactory eventFactory;
	private XMLEventWriter writer;
	private boolean writingOften;

	/**
	 * Creates a new OBFLWsNormalizer. 
	 * @param input the input XMLEventReader. Note that the underlying stream might not be closed after parsing, due to limitations in the StaX implementation.
	 * @param eventFactory
	 * @param out the output stream
	 * @throws XMLStreamException
	 */
	public OBFLWsNormalizer(XMLEventReader input, XMLEventFactory eventFactory, OutputStream out) throws XMLStreamException {
		this.input = input;
		this.writer = null;
		this.out = out;
		this.eventFactory = eventFactory;
		this.writingOften = false;
	}

	public void parse(XMLOutputFactory outputFactory) {
		XMLEvent event;
		while (input.hasNext()) {
			try {
				event = input.nextEvent();
			} catch (XMLStreamException e) {
				// if something goes wrong when reading the next input, it is possible
				// to get stuck in an endless loop if we keep calling nextEvent.
				throw new RuntimeException(e);
			}
			try {
				if (event.getEventType() == XMLStreamConstants.START_DOCUMENT) {
					StartDocument sd = (StartDocument) event;
					if (sd.encodingSet()) {
						writer = outputFactory.createXMLEventWriter(out, sd.getCharacterEncodingScheme());
						writer.add(event);
					} else {
						writer = outputFactory.createXMLEventWriter(out, "utf-8");
						writer.add(eventFactory.createStartDocument("utf-8", "1.0"));
					}
				} else if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
					writer.add(eventFactory.createCharacters(normalizeSpace(event.asCharacters().getData())));
				} else if (beginsMixedContent(event)) {
					parseBlock(event);
				} else {
					writer.add(event);
				}
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		try {
			input.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		//close outer one first
		try {
			writer.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		//should be closed automatically, but it isn't
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isWritingOften() {
		return writingOften;
	}

	public void setWritingOften(boolean writingOften) {
		this.writingOften = writingOften;
	}

	private void parseBlock(XMLEvent event) throws XMLStreamException {
		QName end = event.asStartElement().getName();
		List<XMLEvent> events = new ArrayList<>();
		events.add(event);
		while (input.hasNext()) {
			event = input.nextEvent();
			if (beginsMixedContent(event)) {
				writeEvents(modifyWhitespace(events));
				events.clear();
				parseBlock(event);
			} else if (equalsEnd(event, end)) {
				events.add(event);
				writeEvents(modifyWhitespace(events));
				break;
			} else {
				events.add(event);
			}
		}
	}
	
	private void writeEvents(List<XMLEvent> modified) throws XMLStreamException {
		// write result
		for (XMLEvent event : modified) {
			writer.add(event);
		}
		if (writingOften) {
			writer.flush();
		}
	}

	private List<XMLEvent> modifyWhitespace(List<XMLEvent> events)  {
		List<XMLEvent> modified = new ArrayList<>();
		// process
		for (int i = 0; i < events.size(); i++) {
			XMLEvent event = events.get(i);

			if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
				final String data = event.asCharacters().getData();

				String pre = "";
				String post = "";
				boolean beginWSMatch = beginWS(data);

				if (isSpace(data) && ((i == events.size() - 2 && endsMixedContent(events.get(i + 1))) || i == events.size() - 1)) {
					// this is the last element in the block, ignore
				} else if (i > 0) {
					XMLEvent preceedingEvent = events.get(i - 1);
					if (preceedingEvent.isEndElement() && beginWSMatch && isPreserveElement(preceedingEvent.asEndElement().getName())) {
						pre = " ";
					} else if (equalsEnd(preceedingEvent, ObflQName.SPAN, ObflQName.STYLE) && beginWSMatch) {
						pre = " ";
					} else if (equalsEnd(preceedingEvent, ObflQName.MARKER, ObflQName.ANCHOR)) {
						if (beginWSMatch) {
							pre = " ";
						} else {
							int j = untilEventIsNotBackward(events, i - 1, ObflQName.MARKER, ObflQName.ANCHOR);
							if (j > -1) {
								XMLEvent upstream = events.get(j);
								if (upstream.isCharacters() && endWS(upstream.asCharacters().getData())) {
									pre = " ";
								}
							}
						}
					} else if (preceedingEvent.isEndElement()) {
						int j = untilEventIsNotBackward(events, i - 1, XMLStreamConstants.END_ELEMENT);
						if (j > -1) {
							XMLEvent upstream = events.get(j);
							if (upstream.isCharacters() && endWS(upstream.asCharacters().getData())) {
								pre = " ";
							}
						}
					}

				}
				if (i < events.size() - 1) {
					XMLEvent followingEvent = events.get(i + 1);
					boolean endWSMatch = endWS(data);
					if (isSpace(data)) {
						// don't output post
						if (equalsStart(followingEvent, ObflQName.MARKER)) {
							pre = "";
						}
					} else if (followingEvent.isStartElement() && endWSMatch && isPreserveElement(followingEvent.asStartElement().getName())) {
						post = " ";
					} else if (equalsStart(followingEvent, ObflQName.SPAN, ObflQName.STYLE) && endWSMatch) {
						post = " ";
					} else if (followingEvent.isStartElement()) {
						int j = untilEventIsNotForward(events, i + 1, XMLStreamConstants.START_ELEMENT);
						if (j > -1) {
							XMLEvent downstream = events.get(j);
							if (downstream.isCharacters() && beginWS(downstream.asCharacters().getData())) {
								post = " ";
							}
						}
					}
				}
				// System.out.println("'" + pre + "'" + normalizeSpace(data) +
				// "'" + post + "'");
				modified.add(eventFactory.createCharacters(pre + normalizeSpace(data) + post));
			} else if (equalsStart(event, ObflQName.SPAN, ObflQName.STYLE)) {

				if (i > 0) {
					int j = untilEventIsNotBackward(events, i - 1, ObflQName.MARKER, ObflQName.ANCHOR);
					if (!(j > -1 && j < i - 1)) {
						j = untilEventIsNotBackward(events, i - 1, XMLStreamConstants.END_ELEMENT);
					}
					if (j > -1 && j < i - 1) {
						XMLEvent upstream = events.get(j);
						if (upstream.isCharacters() && endWS(upstream.asCharacters().getData())) {
							modified.add(eventFactory.createCharacters(" "));
						}
					}
				}

				modified.add(event);

			} else if (equalsEnd(event, ObflQName.SPAN, ObflQName.STYLE)) {
				modified.add(event);
				if (i < events.size() - 1) {
					int j = untilEventIsNotForward(events, i + 1, XMLStreamConstants.START_ELEMENT);
					if (j > -1 && j > i + 1) {
						XMLEvent downstream = events.get(j);
						if (downstream.isCharacters() && beginWS(downstream.asCharacters().getData())) {
							modified.add(eventFactory.createCharacters(" "));
						}
					}
				}
			} else {
				modified.add(event);
			}
		}
		return modified;
	}

	private boolean isPreserveElement(QName name) {
		return name.equals(ObflQName.PAGE_NUMBER) || name.equals(ObflQName.LEADER) || name.equals(ObflQName.EVALUATE);
	}

	private static int untilEventIsNotForward(List<XMLEvent> events, final int i, final int eventType) {
		for (int j = i; j < events.size(); j++) {
			if (events.get(j).getEventType() != eventType) {
				return j;
			}
		}
		return -1;
	}

	private static int untilEventIsNotBackward(List<XMLEvent> events, final int i, final int eventType) {
		for (int j = 0; j < i; j++) {
			if (events.get(i - j).getEventType() != eventType) {
				return i - j;
			}
		}
		return -1;
	}

	private static int untilEventIsNotBackward(List<XMLEvent> events, final int i, QName... name) {
		for (int j = 0; j < i; j++) {
			XMLEvent event = events.get(i - j);
			if (XMLParserBase.equalsElement(event, name)) {
				// continue
			} else {
				return i - j;
			}
		}
		return -1;
	}
	
	private boolean beginsMixedContent(XMLEvent event) {
		return equalsStart(event, ObflQName.BLOCK, ObflQName.TOC_ENTRY, ObflQName.ITEM, ObflQName.BEFORE, ObflQName.AFTER, ObflQName.TD);
			   
	}
	
	private boolean endsMixedContent(XMLEvent event) {
		return equalsEnd(event, ObflQName.BLOCK, ObflQName.TOC_ENTRY, ObflQName.ITEM, ObflQName.BEFORE, ObflQName.AFTER, ObflQName.TD);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			XMLInputFactory inFactory = XMLInputFactory.newInstance();
			inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
			inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			OBFLWsNormalizer p = new OBFLWsNormalizer(inFactory.createXMLEventReader(new FileInputStream("ws-test-input.xml")), XMLEventFactory.newInstance(), new FileOutputStream("out.xml"));
			p.parse(XMLOutputFactory.newInstance());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
