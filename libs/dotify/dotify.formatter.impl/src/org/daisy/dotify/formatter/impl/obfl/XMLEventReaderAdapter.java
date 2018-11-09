package org.daisy.dotify.formatter.impl.obfl;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

class XMLEventReaderAdapter implements XMLEventIterator {
	private final XMLEventReader reader;
	
	XMLEventReaderAdapter(XMLEventReader reader) {
		this.reader = reader;
	}

	@Override
	public boolean hasNext() {
		return reader.hasNext();
	}

	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		return reader.nextEvent();
	}

	@Override
	public void close() throws XMLStreamException {
		reader.close();
	}

}
