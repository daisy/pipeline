package org.daisy.dotify.formatter.impl.obfl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

interface XMLEventIterator {
	
	boolean hasNext();
	
	XMLEvent nextEvent() throws XMLStreamException;
	
	void close() throws XMLStreamException;

}
