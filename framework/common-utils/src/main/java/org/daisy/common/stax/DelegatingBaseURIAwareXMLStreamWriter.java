package org.daisy.common.stax;

import java.net.URI;

import javax.xml.stream.XMLStreamException;

public abstract class DelegatingBaseURIAwareXMLStreamWriter extends DelegatingXMLStreamWriter implements BaseURIAwareXMLStreamWriter {

	@Override
	protected abstract BaseURIAwareXMLStreamWriter delegate();

	@Override
	public void setBaseURI(URI baseURI) throws XMLStreamException {
		delegate().setBaseURI(baseURI);
	}

	@Override
	public URI getBaseURI() throws XMLStreamException {
		return delegate().getBaseURI();
	}
}
