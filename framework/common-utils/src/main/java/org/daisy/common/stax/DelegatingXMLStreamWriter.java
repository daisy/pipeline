package org.daisy.common.stax;

import java.net.URI;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

public abstract class DelegatingXMLStreamWriter implements BaseURIAwareXMLStreamWriter {

	protected abstract BaseURIAwareXMLStreamWriter delegate();

	public void writeStartElement(String localName) throws XMLStreamException {
		delegate().writeStartElement(localName);
	}

	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		delegate().writeStartElement(namespaceURI, localName);
	}

	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		delegate().writeStartElement(prefix, localName, namespaceURI);
	}

	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		delegate().writeEmptyElement(namespaceURI, localName);
	}

	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		delegate().writeEmptyElement(prefix, localName, namespaceURI);
	}

	public void writeEmptyElement(String localName) throws XMLStreamException {
		delegate().writeEmptyElement(localName);
	}

	public void writeEndElement() throws XMLStreamException {
		delegate().writeEndElement();
	}

	public void writeEndDocument() throws XMLStreamException {
		delegate().writeEndDocument();
	}

	public void flush() throws XMLStreamException {
		delegate().flush();
	}

	public void close() throws XMLStreamException {
		delegate().close();
	}

	public void writeAttribute(String localName, String value) throws XMLStreamException {
		delegate().writeAttribute(localName, value);
	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
		delegate().writeAttribute(prefix, namespaceURI, localName, value);
	}

	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		delegate().writeAttribute(namespaceURI, localName, value);
	}

	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		delegate().writeNamespace(prefix, namespaceURI);
	}

	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		delegate().writeDefaultNamespace(namespaceURI);
	}

	public void writeComment(String data) throws XMLStreamException {
		delegate().writeComment(data);
	}

	public void writeProcessingInstruction(String target) throws XMLStreamException {
		delegate().writeProcessingInstruction(target);
	}

	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		delegate().writeProcessingInstruction(target, data);
	}

	public void writeCData(String data) throws XMLStreamException {
		delegate().writeCData(data);
	}

	public void writeDTD(String dtd) throws XMLStreamException {
		delegate().writeDTD(dtd);
	}

	public void writeEntityRef(String name) throws XMLStreamException {
		delegate().writeEntityRef(name);
	}

	public void writeStartDocument() throws XMLStreamException {
		delegate().writeStartDocument();
	}

	public void writeStartDocument(String version) throws XMLStreamException {
		delegate().writeStartDocument(version);
	}

	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		delegate().writeStartDocument(encoding, version);
	}

	public void writeCharacters(String text) throws XMLStreamException {
		delegate().writeCharacters(text);
	}

	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		delegate().writeCharacters(text, start, len);
	}

	public String getPrefix(String uri) throws XMLStreamException {
		return delegate().getPrefix(uri);
	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		delegate().setPrefix(prefix, uri);
	}

	public void setDefaultNamespace(String uri) throws XMLStreamException {
		delegate().setDefaultNamespace(uri);
	}

	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		delegate().setNamespaceContext(context);
	}

	public NamespaceContext getNamespaceContext() {
		return delegate().getNamespaceContext();
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		return delegate().getProperty(name);
	}

	public URI getBaseURI() throws XMLStreamException {
		return delegate().getBaseURI();
	}

	public void setBaseURI(URI baseURI) throws XMLStreamException {
		delegate().setBaseURI(baseURI);
	}
}
