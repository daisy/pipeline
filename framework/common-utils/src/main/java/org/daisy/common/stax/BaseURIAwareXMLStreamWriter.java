package org.daisy.common.stax;

import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface BaseURIAwareXMLStreamWriter extends XMLStreamWriter {
	
	public URI getBaseURI() throws XMLStreamException;
	
	public void setBaseURI(URI baseURI) throws XMLStreamException;
	
}
