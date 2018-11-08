package org.daisy.common.stax;

import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface BaseURIAwareXMLStreamReader extends XMLStreamReader {
	
	public URI getBaseURI() throws XMLStreamException;
	
}
