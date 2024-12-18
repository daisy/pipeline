package org.daisy.common.saxon;

import java.net.URI;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.DelegatingBaseURIAwareXMLStreamWriter;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLOutputValue;

import org.w3c.dom.Node;

public class SaxonOutputValue extends XMLOutputValue<Void> {

	private SaxonOutputValue backingValue = null;
	private final Consumer<XdmItem> xdmItemConsumer;
	private final Configuration configuration;

	public SaxonOutputValue(Consumer<XdmItem> value, Configuration configuration) {
		super(createXMLStreamWriter(value, configuration));
		xdmItemConsumer = value;
		this.configuration = configuration;
	}

	public SaxonOutputValue(SaxonOutputValue value) {
		super(value);
		backingValue = value;
		xdmItemConsumer = null; // will not be accessed
		configuration = null; // will not be accessed
	}

	public Consumer<XdmItem> asXdmItemConsumer() {
		if (backingValue != null)
			return backingValue.asXdmItemConsumer();
		else
			return xdmItemConsumer;
	}

	public Consumer<Node> asNodeConsumer() {
		if (backingValue != null)
			return backingValue.asNodeConsumer();
		else
			return n -> xdmItemConsumer.accept(domToXdmNode(n));
	}

	@Override
	public BaseURIAwareXMLStreamWriter asXMLStreamWriter() {
		return super.asXMLStreamWriter();
	}

	private XdmNode domToXdmNode(Node node) {
		if (node instanceof NodeOverNodeInfo) {
			NodeInfo nodeInfo = ((NodeOverNodeInfo)node).getUnderlyingNodeInfo();
			if (configuration.equals(nodeInfo.getConfiguration()))
				return new XdmNode(nodeInfo);
		}
		try {
			return new Processor(configuration).newDocumentBuilder().build(new DOMSource(node));
		} catch (SaxonApiException e) {
			throw new TransformerException(e);
		}
	}

	private static BaseURIAwareXMLStreamWriter createXMLStreamWriter(Consumer<XdmItem> itemConsumer, Configuration config) {
		return new DelegatingBaseURIAwareXMLStreamWriter() {
			private BaseURIAwareXMLStreamWriter writer = null;
			private Receiver receiver;
			// "An XdmDestination is designed to hold a single tree rooted at a document node."
			private XdmDestination destination;
			private boolean seenStartDocument = false;
			private int elementDepth = 0;
			protected BaseURIAwareXMLStreamWriter delegate() {
				if (writer == null) {
					try {
						destination = new XdmDestination();
						receiver = new NamespaceReducer(destination.getReceiver(config));
						receiver.open();
						writer = new BaseURIAwareStreamWriterToReceiver(receiver);
					} catch (SaxonApiException | XPathException e) {
						throw new TransformerException(e);
					}
				}
				return writer;
			}
			@Override
			public void writeStartDocument() throws XMLStreamException {
				if (elementDepth != 0)
					throw new XMLStreamException();
				if (seenStartDocument)
					throw new XMLStreamException();
				super.writeStartDocument();
				seenStartDocument = true;
			}
			@Override
			public void writeStartDocument(String version) throws XMLStreamException {
				if (elementDepth != 0)
					throw new XMLStreamException();
				if (seenStartDocument)
					throw new XMLStreamException();
				super.writeStartDocument(version);
				seenStartDocument = true;
			}
			@Override
			public void writeStartDocument(String encoding, String version) throws XMLStreamException {
				if (elementDepth != 0)
					throw new XMLStreamException();
				if (seenStartDocument)
					throw new XMLStreamException();
				super.writeStartDocument(encoding, version);
				seenStartDocument = true;
			}
			@Override
			public void writeStartElement(String localName) throws XMLStreamException {
				super.writeStartElement(localName);
				elementDepth++;
			}
			@Override
			public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
				super.writeStartElement(namespaceURI, localName);
				elementDepth++;
			}
			@Override
			public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
				super.writeStartElement(prefix, localName, namespaceURI);
				elementDepth++;
			}
			@Override
			public void writeAttribute(String localName, String value) throws XMLStreamException {
				if (elementDepth == 0) {
					try {
						super.writeStartElement("_");
						super.writeAttribute(localName, value);
						super.writeEndElement();
						receiver.close();
						XdmNode doc = destination.getXdmNode();
						itemConsumer.accept(((XdmNode)doc.axisIterator(Axis.CHILD).next()).axisIterator(Axis.ATTRIBUTE).next());
					} catch (XPathException e) {
						throw new XMLStreamException(e);
					}
					seenStartDocument = false;
					writer = null;
				} else
					super.writeAttribute(localName, value);
			}
			@Override
			public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
				if (elementDepth == 0) {
					try {
						super.writeStartElement("_");
						super.writeAttribute(namespaceURI, localName, value);
						super.writeEndElement();
						receiver.close();
						XdmNode doc = destination.getXdmNode();
						itemConsumer.accept(((XdmNode)doc.axisIterator(Axis.CHILD).next()).axisIterator(Axis.ATTRIBUTE).next());
					} catch (XPathException e) {
						throw new XMLStreamException(e);
					}
					seenStartDocument = false;
					writer = null;
				} else
					super.writeAttribute(namespaceURI, localName, value);
			}
			@Override
			public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
				if (elementDepth == 0) {
					try {
						super.writeStartElement("_");
						super.writeAttribute(prefix, namespaceURI, localName, value);
						super.writeEndElement();
						receiver.close();
						XdmNode doc = destination.getXdmNode();
						itemConsumer.accept(((XdmNode)doc.axisIterator(Axis.CHILD).next()).axisIterator(Axis.ATTRIBUTE).next());
					} catch (XPathException e) {
						throw new XMLStreamException(e);
					}
					seenStartDocument = false;
					writer = null;
				} else
					super.writeAttribute(prefix, namespaceURI, localName, value);
			}
			@Override
			public void writeEndElement() throws XMLStreamException {
				if (elementDepth == 0)
					throw new XMLStreamException();
				super.writeEndElement();
				elementDepth--;
				if (!seenStartDocument && elementDepth == 0) {
					try {
						receiver.close();
						XdmNode doc = destination.getXdmNode();
						itemConsumer.accept(doc.axisIterator(Axis.CHILD).next());
					} catch (XPathException e) {
						throw new XMLStreamException(e);
					}
					seenStartDocument = false;
					writer = null;
				}
			}
			@Override
			public void writeEndDocument() throws XMLStreamException {
				if (elementDepth != 0)
					throw new XMLStreamException();
				if (!seenStartDocument)
					throw new XMLStreamException();
				super.writeEndDocument();
				try {
					receiver.close();
					itemConsumer.accept(destination.getXdmNode());
				} catch (XPathException e) {
					throw new XMLStreamException(e);
				}
				seenStartDocument = false;
				writer = null;
			}
		};
	}

	private static class BaseURIAwareStreamWriterToReceiver extends StreamWriterToReceiver
		implements BaseURIAwareXMLStreamWriter {

		private final Receiver receiver;
		// FIXME: change when xml:base attributes are written
		private URI baseURI = null;
		private boolean seenRoot = false;

		public BaseURIAwareStreamWriterToReceiver(Receiver receiver) {
			super(receiver);
			this.receiver = receiver;
		}

		public URI getBaseURI() throws XMLStreamException {
			return baseURI;
		}

		public void setBaseURI(URI baseURI) throws XMLStreamException {
			if (seenRoot)
				throw new XMLStreamException("Setting base URI not supported after document has started.");
			if (baseURI != null)
				receiver.setSystemId(baseURI.toASCIIString());
			this.baseURI = baseURI;
		}

		@Override
		public void writeStartDocument() throws XMLStreamException {
			super.writeStartDocument();
			seenRoot = true;
		}
	}
}
