package org.daisy.common.stax;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

public final class XMLStreamWriterHelper {
	
	private static QName nodeName(Node node) {
		String prefix = node.getPrefix();
		String ns = node.getNamespaceURI();
		String localPart = node.getLocalName();
		if (prefix != null)
			return new QName(ns, localPart, prefix);
		else
			return new QName(ns, localPart);
	}
	
	public static Map<QName,String> getAttributes(XMLStreamReader reader) {
		Map<QName,String> map = new HashMap<QName,String>();
		for (int i = 0; i < reader.getAttributeCount(); i++)
			map.put(reader.getAttributeName(i), reader.getAttributeValue(i));
		return map;
	}
	
	public static void writeAttribute(XMLStreamWriter writer, QName name, String value) throws XMLStreamException {
		writeAttribute(writer, name, value, false);
	}
	
	public static void writeAttribute(XMLStreamWriter writer, QName name, String value, boolean writeNamespaceNodes)
			throws XMLStreamException {
		String prefix = name.getPrefix();
		String ns = name.getNamespaceURI();
		String localPart = name.getLocalPart();
		if ("http://www.w3.org/2000/xmlns/".equals(ns)) {
			if (!writeNamespaceNodes)
				return;
		}
		if (prefix == null || "".equals(prefix))
			writer.writeAttribute(ns, localPart, value);
		else
			writer.writeAttribute(prefix, ns, localPart, value);
	}
	
	public static void writeAttribute(XMLStreamWriter writer, Node attr) throws XMLStreamException {
		writeAttribute(writer, attr, false);
	}
	
	public static void writeAttribute(XMLStreamWriter writer, Node attr, boolean copyNamespaceNodes) throws XMLStreamException {
		writeAttribute(writer, nodeName(attr), attr.getNodeValue(), copyNamespaceNodes);
	}
	
	public static void writeAttribute(XMLStreamWriter writer, Map.Entry<QName,String> attribute) throws XMLStreamException {
		writeAttribute(writer, attribute.getKey(), attribute.getValue());
	}
	
	public static void writeAttributes(XMLStreamWriter writer, Element element) throws XMLStreamException {
		writeAttributes(writer, element, false);
	}
	
	public static void writeAttributes(XMLStreamWriter writer, Element element, boolean copyNamespaceNodes) throws XMLStreamException {
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attr = attributes.item(i);
			if (attr.getLocalName() == null) continue; // when is this the case?
			writeAttribute(writer, attr, copyNamespaceNodes);
		}
	}
	
	public static void writeAttributes(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		for (int i = 0; i < reader.getAttributeCount(); i++)
			writeAttribute(writer, reader.getAttributeName(i), reader.getAttributeValue(i));
	}
	
	public static void writeAttributes(XMLStreamWriter writer, Map<QName,String> attributes) throws XMLStreamException {
		for (Map.Entry<QName,String> attr : attributes.entrySet())
			writeAttribute(writer, attr);
	}
	
	public static void writeCData(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		writer.writeCData(reader.getText());
	}
	
	public static void writeCharacters(XMLStreamWriter writer, Node text) throws XMLStreamException {
		writer.writeCharacters(text.getNodeValue());
	}
	
	public static void writeCharacters(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		writer.writeCharacters(reader.getText());
	}
	
	public static void writeComment(XMLStreamWriter writer, Node node) throws XMLStreamException {
		writer.writeComment(node.getNodeValue());
	}
	
	public static void writeComment(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		writer.writeComment(reader.getText());
	}
	
	public static void writeDocument(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		writer.writeStartDocument();
	  loop: while (true)
			try {
				int event = reader.next();
				switch (event) {
				case START_DOCUMENT:
					break;
				case END_DOCUMENT:
					break loop;
				case START_ELEMENT:
					writeElement(writer, reader);
					break;
				default:
					writeEvent(writer, reader);
				}
			} catch (NoSuchElementException e) {
				break;
			}
		writer.writeEndDocument();
	}
	
	public static void writeElement(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		writeStartElement(writer, reader.getName());
		writeAttributes(writer, reader);
		int depth = 0;
		while (true)
			try {
				int event = reader.next();
				switch (event) {
				case START_ELEMENT:
					writeStartElement(writer, reader);
					writeAttributes(writer, reader);
					depth++;
					break;
				case END_ELEMENT:
					writer.writeEndElement();
					depth--;
					if (depth < 0)
						return;
					break;
				default:
					writeEvent(writer, reader); }}
			catch (NoSuchElementException e) {
				throw new RuntimeException("coding error"); }
	}
	
	public static void writeElement(XMLStreamWriter writer, Element element) throws XMLStreamException {
		writeStartElement(writer, element, true, false, false);
		for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
			writeNode(writer, child);
		writer.writeEndElement();
	}
	
	public static void writeDocument(XMLStreamWriter writer, Document document) throws XMLStreamException {
		writeDocument(writer, document, false);
	}
	
	public static void writeDocument(XMLStreamWriter writer, Document document, boolean copyBaseURI) throws XMLStreamException {
		if (copyBaseURI) {
			if (writer instanceof BaseURIAwareXMLStreamWriter) {
				String baseURI = document.getBaseURI();
				((BaseURIAwareXMLStreamWriter)writer).setBaseURI(baseURI == null ? null : URI.create(baseURI));
			} else
				throw new IllegalArgumentException();
		}
		writer.writeStartDocument();
		writeElement(writer, document.getDocumentElement());
		writer.writeEndDocument();
	}
	
	public static void writeNode(XMLStreamWriter writer, Node node) throws XMLStreamException {
		if (node.getNodeType() == Node.ELEMENT_NODE)
			writeElement(writer, (Element)node);
		else if (node.getNodeType() == Node.COMMENT_NODE)
			writeComment(writer, node);
		else if (node.getNodeType() == Node.TEXT_NODE)
			writeCharacters(writer, node);
		else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
			writeProcessingInstruction(writer, node);
		else
			throw new UnsupportedOperationException("Unexpected node type");
	}
	
	public static void writeEvent(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		switch (reader.getEventType()) {
		case START_DOCUMENT:
			writer.writeStartDocument();
			break;
		case END_DOCUMENT:
			writer.writeEndDocument();
			break;
		case START_ELEMENT:
			writeStartElement(writer, reader);
			break;
		case END_ELEMENT:
			writer.writeEndElement();
			break;
		case SPACE:
		case CHARACTERS:
			writeCharacters(writer, reader);
			break;
		case PROCESSING_INSTRUCTION:
			writeProcessingInstruction(writer, reader);
			break;
		case CDATA:
			writeCData(writer, reader);
			break;
		case COMMENT:
			writeComment(writer, reader);
			break;
		}
	}
	
	public static void writeProcessingInstruction(XMLStreamWriter writer, Node pi) throws XMLStreamException {
		writer.writeProcessingInstruction(pi.getLocalName(), pi.getNodeValue());
	}
	
	public static void writeProcessingInstruction(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		String target = reader.getPITarget();
		String data = reader.getPIData();
		if (data == null)
			writer.writeProcessingInstruction(target);
		else
			writer.writeProcessingInstruction(target, data);
	}
	
	public static void writeStartElement(XMLStreamWriter writer, QName name) throws XMLStreamException {
		String prefix = name.getPrefix();
		String ns = name.getNamespaceURI();
		String localPart = name.getLocalPart();
		if (prefix != null)
			writer.writeStartElement(prefix, localPart, ns);
		else
			writer.writeStartElement(ns, localPart);
	}
	
	public static void writeStartElement(XMLStreamWriter writer, XMLStreamReader reader) throws XMLStreamException {
		writeStartElement(writer, reader.getName());
	}
	
	public static void writeStartElement(XMLStreamWriter writer, Element element) throws XMLStreamException {
		writeStartElement(writer, element, false, false, false);
	}
	
	public static void writeStartElement(XMLStreamWriter writer, Element element,
	                                     boolean copyAttributes, boolean copyNamespaceNodes, boolean copyBaseURI)
			throws XMLStreamException {
		String prefix = element.getPrefix();
		String ns = element.getNamespaceURI();
		String localPart = element.getLocalName();
		if (prefix != null)
			writer.writeStartElement(prefix, localPart, ns);
		else if (ns != null)
			writer.writeStartElement(ns, localPart);
		else
			writer.writeStartElement(localPart);
		if (copyAttributes) {
			writeAttributes(writer, element, copyNamespaceNodes);
		}
		if (copyBaseURI) {
			if (writer instanceof BaseURIAwareXMLStreamWriter) {
				String baseURI = element.getBaseURI();
				((BaseURIAwareXMLStreamWriter)writer).setBaseURI(baseURI == null ? null : URI.create(baseURI));
			} else
				throw new IllegalArgumentException();
		}
	}
	
	public interface WriterEvent {
		public void writeTo(XMLStreamWriter writer) throws XMLStreamException;
	}
	
	public interface FutureWriterEvent extends WriterEvent {
		public boolean isReady();
	}
	
	public static class BufferedXMLStreamWriter implements BaseURIAwareXMLStreamWriter {
		
		private final BaseURIAwareXMLStreamWriter zuper;
		
		public BufferedXMLStreamWriter(BaseURIAwareXMLStreamWriter zuper) {
			this.zuper = zuper;
		}
		
		private Queue<WriterEvent> queue = new LinkedList<>();
		
		public void writeEvent(FutureWriterEvent event) throws XMLStreamException {
			queue.add(event);
			flushQueue();
		}
		
		private boolean isQueueEmpty() {
			return queue == null || queue.isEmpty();
		}
		
		private boolean flushQueue() throws XMLStreamException {
			if (queue == null)
				return true;
			List<WriterEvent> todo = null;
			while (!queue.isEmpty()) {
				WriterEvent event = queue.peek();
				if (event instanceof FutureWriterEvent && !((FutureWriterEvent)event).isReady())
					break;
				if (todo == null)
					todo = new ArrayList<WriterEvent>();
				todo.add(event);
				queue.remove(); }
			Queue<WriterEvent> tmp = queue;
			queue = null;
			if (todo != null)
				for (WriterEvent event : todo)
					event.writeTo(this);
			queue = tmp;
			return queue.isEmpty();
		}

		@Override
		public void flush() throws XMLStreamException {
			if (!flushQueue())
				throw new XMLStreamException("not ready");
			zuper.flush();
		}

		@Override
		public void close() throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public URI getBaseURI() throws XMLStreamException {
			return zuper.getBaseURI();
		}
		
		@Override
		public NamespaceContext getNamespaceContext() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPrefix(String uri) throws XMLStreamException {
			if (!isQueueEmpty())
				throw new IllegalStateException();
			return zuper.getPrefix(uri);
		}

		@Override
		public Object getProperty(String name) throws IllegalArgumentException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setBaseURI(URI baseURI) throws XMLStreamException {
			zuper.setBaseURI(baseURI);
		}

		@Override
		public void setDefaultNamespace(String uri) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setPrefix(String prefix, String uri) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void writeAttribute(String localName, String value) throws XMLStreamException {
			if (flushQueue())
				zuper.writeAttribute(localName, value);
			else
				queue.add(w -> w.writeAttribute(localName, value));
		}

		@Override
		public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
			if (flushQueue())
				zuper.writeAttribute(prefix, namespaceURI, localName, value);
			else
				queue.add(w -> w.writeAttribute(prefix, namespaceURI, localName, value));
		}

		@Override
		public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
			if (flushQueue())
				zuper.writeAttribute(namespaceURI, localName, value);
			else
				queue.add(w -> w.writeAttribute(namespaceURI, localName, value));
		}
		
		@Override
		public void writeCData(String text) throws XMLStreamException {
			if (flushQueue())
				zuper.writeCData(text);
			else
				queue.add(w -> w.writeCData(text));
		}

		@Override
		public void writeCharacters(String text) throws XMLStreamException {
			if (flushQueue())
				zuper.writeCharacters(text);
			else
				queue.add(w -> w.writeCharacters(text));
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void writeComment(String text) throws XMLStreamException {
			if (flushQueue())
				zuper.writeComment(text);
			else
				queue.add(w -> w.writeComment(text));
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
			if (flushQueue())
				zuper.writeDefaultNamespace(namespaceURI);
			else
				queue.add(w -> w.writeDefaultNamespace(namespaceURI));
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEmptyElement(String localName) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void writeEndElement() throws XMLStreamException {
			if (flushQueue())
				zuper.writeEndElement();
			else
				queue.add(w -> w.writeEndElement());
		}
		
		@Override
		public void writeEndDocument() throws XMLStreamException {
			if (flushQueue())
				zuper.writeEndDocument();
			else
				queue.add(w -> w.writeEndDocument());
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
			if (flushQueue())
				zuper.writeNamespace(prefix, namespaceURI);
			else
				queue.add(w -> w.writeNamespace(prefix, namespaceURI));
		}
		
		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException {
			if (flushQueue())
				zuper.writeProcessingInstruction(target);
			else
				queue.add(w -> w.writeProcessingInstruction(target));
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
			if (flushQueue())
				zuper.writeProcessingInstruction(target, data);
			else
				queue.add(w -> w.writeProcessingInstruction(target, data));
		}
		
		@Override
		public void writeStartDocument() throws XMLStreamException {
			if (flushQueue())
				zuper.writeStartDocument();
			else
				queue.add(w -> w.writeStartDocument());
		}

		@Override
		public void writeStartDocument(String version) throws XMLStreamException {
			if (flushQueue())
				zuper.writeStartDocument(version);
			else
				queue.add(w -> w.writeStartDocument(version));
		}

		@Override
		public void writeStartDocument(String encoding, String version) throws XMLStreamException {
			if (flushQueue())
				zuper.writeStartDocument(encoding, version);
			else
				queue.add(w -> w.writeStartDocument(encoding, version));
		}
		
		@Override
		public void writeStartElement(String localName) throws XMLStreamException {
			if (flushQueue())
				zuper.writeStartElement(localName);
			else
				queue.add(w -> w.writeStartElement(localName));
		}

		@Override
		public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
			if (flushQueue())
				zuper.writeStartElement(namespaceURI, localName);
			else
				queue.add(w -> w.writeStartElement(namespaceURI, localName));
		}

		@Override
		public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			if (flushQueue())
				zuper.writeStartElement(prefix, localName, namespaceURI);
			else
				queue.add(w -> w.writeStartElement(prefix, localName, namespaceURI));
		}
	}
	
	public static class ToStringWriter implements BaseURIAwareXMLStreamWriter {
		
		private StringBuilder b = new StringBuilder();
		
		private Stack<String> elements = new Stack<String>();
		private boolean startTagOpen = false;
		
		@Override
		public String toString() {
			return b.toString();
		}

		@Override
		public void close() throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void flush() throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public URI getBaseURI() throws XMLStreamException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public NamespaceContext getNamespaceContext() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPrefix(String uri) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getProperty(String name) throws IllegalArgumentException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setBaseURI(URI baseURI) throws XMLStreamException {
		}

		@Override
		public void setDefaultNamespace(String uri) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setPrefix(String prefix, String uri) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeAttribute(String localName, String value) throws XMLStreamException {
			writeAttribute(null, localName, value);
		}

		@Override
		public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
			b.append(" ").append(localName).append("='").append(value).append("'");
		}

		@Override
		public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
			writeAttribute(null, namespaceURI, localName, value);
		}

		@Override
		public void writeCData(String data) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void writeCharacters(String text) throws XMLStreamException {
			if (startTagOpen) {
				b.append(">");
				startTagOpen = false; }
			b.append(text);
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeComment(String data) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEmptyElement(String localName) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEndDocument() throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEndElement() throws XMLStreamException {
			if (startTagOpen) {
				b.append("/>");
				startTagOpen = false;
				elements.pop(); }
			else
				b.append("</").append(elements.pop()).append(">");
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		}

		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeStartDocument() throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeStartDocument(String version) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeStartDocument(String encoding, String version) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void writeStartElement(String localName) throws XMLStreamException {
			writeStartElement(null, localName);
		}

		@Override
		public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
			writeStartElement(null, namespaceURI, localName);
		}

		@Override
		public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			if (startTagOpen) {
				b.append(">");
				startTagOpen = false; }
			elements.push(localName);
			b.append("<").append(localName);
			startTagOpen = true;
		}
	}
	
	private XMLStreamWriterHelper() {
		// no instantiation
	}
}
