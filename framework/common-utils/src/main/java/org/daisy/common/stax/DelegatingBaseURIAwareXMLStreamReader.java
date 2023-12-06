package org.daisy.common.stax;

import java.net.URI;

import javax.xml.stream.XMLStreamException;

public abstract class DelegatingBaseURIAwareXMLStreamReader extends DelegatingXMLStreamReader implements BaseURIAwareXMLStreamReader {

	@Override
	protected abstract BaseURIAwareXMLStreamReader delegate();

	@Override
	public URI getBaseURI() throws XMLStreamException {
		return delegate().getBaseURI();
	}
}
