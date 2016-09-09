package org.daisy.pipeline.braille.common.saxon;

import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.evpull.Decomposer;
import net.sf.saxon.evpull.EventIteratorOverSequence;
import net.sf.saxon.evpull.EventToStaxBridge;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.braille.common.TransformationException;

public abstract class StreamToStreamTransform {
	
	private final XPathContext context;
	
	public StreamToStreamTransform(XPathContext context) {
		this.context = context;
	}
	
	protected abstract void _transform(XMLStreamReader reader, Writer writer) throws TransformationException;
	
	public final XdmNode transform(NodeInfo element) throws TransformationException {
		try {
			PipelineConfiguration pipeConfig = new PipelineConfiguration(context.getConfiguration());
			XMLStreamReader reader
				= new EventToStaxBridge(
					new Decomposer(
						new EventIteratorOverSequence(element.iterate()), pipeConfig), pipeConfig);
			XdmDestination destination = new XdmDestination();
			Receiver receiver = destination.getReceiver(context.getConfiguration());
			receiver.open();
			WriterImpl writer = new WriterImpl(receiver);
			_transform(reader, writer);
			receiver.close();
			return (XdmNode)destination.getXdmNode().axisIterator(Axis.CHILD).next();
		} catch (Exception e) {
			throw new TransformationException(e);
		}
	}
	
	protected interface Writer extends XMLStreamWriter {
		public void writeStartElement(QName name) throws XMLStreamException;
		public void writeAttribute(QName name, String value) throws XMLStreamException;
	}
	
	private static class WriterImpl extends StreamWriterToReceiver implements Writer {
		
		WriterImpl(Receiver receiver) {
			super(receiver);
		}
		
		public void writeStartElement(QName name) throws XMLStreamException {
			writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
		}
		
		public void writeAttribute(QName name, String value) throws XMLStreamException {
			writeAttribute(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), value);
		}
	}
	
	public static abstract class util {
		
		public static class ToStringWriter implements Writer {
			
			StringBuilder b = new StringBuilder();
			
			Stack<String> elements = new Stack<String>();
			boolean startTagOpen = false;
			
			@Override
			public String toString() {
				return b.toString();
			}
			
			public void writeStartElement(QName name) throws XMLStreamException {
				writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
			}
			
			public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
				if (startTagOpen) {
					b.append(">");
					startTagOpen = false; }
				elements.push(localName);
				b.append("<").append(localName);
				startTagOpen = true;
			}
			
			public void writeEndElement() throws XMLStreamException {
				if (startTagOpen) {
					b.append("/>");
					startTagOpen = false;
					elements.pop(); }
				else
					b.append("</").append(elements.pop()).append(">");
			}
			
			public void writeAttribute(QName name, String value) throws XMLStreamException {
				writeAttribute(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), value);
			}
			
			public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
				b.append(" ").append(localName).append("='").append(value).append("'");
			}
			
			public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {}
			
			public void writeCharacters(String text) throws XMLStreamException {
				if (startTagOpen) {
					b.append(">");
					startTagOpen = false; }
				b.append(text);
			}
			
			public void writeStartElement(String localName) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeEmptyElement(String localName) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeEndDocument() throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void close() throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void flush() throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeAttribute(String localName, String value) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeComment(String data) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeProcessingInstruction(String target) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeCData(String data) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeDTD(String dtd) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeEntityRef(String name) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeStartDocument() throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeStartDocument(String version) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeStartDocument(String encoding, String version) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public String getPrefix(String uri) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void setPrefix(String prefix, String uri) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void setDefaultNamespace(String uri) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
				throw new UnsupportedOperationException(); }
			public NamespaceContext getNamespaceContext() {
				throw new UnsupportedOperationException(); }
			public Object getProperty(String name) throws IllegalArgumentException {
				throw new UnsupportedOperationException(); }
		}
	}
}
