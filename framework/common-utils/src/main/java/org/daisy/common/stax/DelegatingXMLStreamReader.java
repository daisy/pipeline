package org.daisy.common.stax;

import java.net.URI;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

public abstract class DelegatingXMLStreamReader implements BaseURIAwareXMLStreamReader {

	protected abstract BaseURIAwareXMLStreamReader delegate();

	public Object getProperty(String name) throws IllegalArgumentException {
		return delegate().getProperty(name);
	}

	public int next() throws XMLStreamException {
		return delegate().next();
	}

	public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
		delegate().require(type, namespaceURI, localName);
	}

	public String getElementText() throws XMLStreamException {
		return delegate().getElementText();
	}

	public int nextTag() throws XMLStreamException {
		return delegate().nextTag();
	}

	public boolean hasNext() throws XMLStreamException {
		return delegate().hasNext();
	}

	public void close() throws XMLStreamException {
		delegate().close();
	}

	public String getNamespaceURI(String prefix) {
		return delegate().getNamespaceURI(prefix);
	}

	public boolean isStartElement() {
		return delegate().isStartElement();
	}

	public boolean isEndElement() {
		return delegate().isEndElement();
	}

	public boolean isCharacters() {
		return delegate().isCharacters();
	}

	public boolean isWhiteSpace() {
		return delegate().isWhiteSpace();
	}

	public String getAttributeValue(String namespaceURI, String localName) {
		return delegate().getAttributeValue(namespaceURI, localName);
	}

	public int getAttributeCount() {
		return delegate().getAttributeCount();
	}

	public QName getAttributeName(int index) {
		return delegate().getAttributeName(index);
	}

	public String getAttributeNamespace(int index) {
		return delegate().getAttributeNamespace(index);
	}

	public String getAttributeLocalName(int index) {
		return delegate().getAttributeLocalName(index);
	}

	public String getAttributePrefix(int index) {
		return delegate().getAttributePrefix(index);
	}

	public String getAttributeType(int index) {
		return delegate().getAttributeType(index);
	}

	public String getAttributeValue(int index) {
		return delegate().getAttributeValue(index);
	}

	public boolean isAttributeSpecified(int index) {
		return delegate().isAttributeSpecified(index);
	}

	public int getNamespaceCount() {
		return delegate().getNamespaceCount();
	}

	public String getNamespacePrefix(int index) {
		return delegate().getNamespacePrefix(index);
	}

	public String getNamespaceURI(int index) {
		return delegate().getNamespaceURI(index);
	}

	public NamespaceContext getNamespaceContext() {
		return delegate().getNamespaceContext();
	}

	public int getEventType() {
		return delegate().getEventType();
	}

	public String getText() {
		return delegate().getText();
	}

	public char[] getTextCharacters() {
		return delegate().getTextCharacters();
	}

	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
		return delegate().getTextCharacters(sourceStart, target, targetStart, length);
	}

	public int getTextStart() {
		return delegate().getTextStart();
	}

	public int getTextLength() {
		return delegate().getTextLength();
	}

	public String getEncoding() {
		return delegate().getEncoding();
	}

	public boolean hasText() {
		return delegate().hasText();
	}

	public Location getLocation() {
		return delegate().getLocation();
	}

	public QName getName() {
		return delegate().getName();
	}

	public String getLocalName() {
		return delegate().getLocalName();
	}

	public boolean hasName() {
		return delegate().hasName();
	}

	public String getNamespaceURI() {
		return delegate().getNamespaceURI();
	}

	public String getPrefix() {
		return delegate().getPrefix();
	}

	public String getVersion() {
		return delegate().getVersion();
	}

	public boolean isStandalone() {
		return delegate().isStandalone();
	}

	public boolean standaloneSet() {
		return delegate().standaloneSet();
	}

	public String getCharacterEncodingScheme() {
		return delegate().getCharacterEncodingScheme();
	}

	public String getPITarget() {
		return delegate().getPITarget();
	}

	public String getPIData() {
		return delegate().getPIData();
	}

	public URI getBaseURI() throws XMLStreamException {
		return delegate().getBaseURI();
	}
}