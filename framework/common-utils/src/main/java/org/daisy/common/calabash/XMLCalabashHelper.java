package org.daisy.common.calabash;

import java.net.URI;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

import com.google.common.collect.Iterators;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.saxon.NodeToXMLStreamTransformer;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.transform.DOMToXMLStreamTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLStreamToXMLStreamTransformer;

public final class XMLCalabashHelper {
	
	public static Iterator<XdmNode> readPipe(ReadablePipe pipe) {
		return new Iterator<XdmNode>() {
			public boolean hasNext() {
				return pipe.moreDocuments();
			}
			public XdmNode next() throws TransformerException {
				try {
					return pipe.read();
				} catch (SaxonApiException e) {
					throw new TransformerException(e);
				}
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static void writePipe(WritablePipe pipe, Iterator<XdmNode> output) {
		while (output.hasNext())
			pipe.write(output.next());
	}
	
	public static void transform(XMLStreamToXMLStreamTransformer transformer, ReadablePipe input, WritablePipe output, XProcRuntime runtime)
			throws TransformerException {
		writePipe(output,
		          SaxonHelper.transform(transformer,
		                                Iterators.transform(readPipe(input), XdmNode::getUnderlyingNode),
		                                runtime.getProcessor().getUnderlyingConfiguration()));
	}
	
	public static void transform(DOMToXMLStreamTransformer transformer, ReadablePipe input, WritablePipe output, XProcRuntime runtime)
			throws TransformerException {
		writePipe(output,
		          SaxonHelper.transform(transformer,
		                                Iterators.transform(readPipe(input), XdmNode::getUnderlyingNode),
		                                runtime.getProcessor().getUnderlyingConfiguration()));
	}
	
	public static void transform(NodeToXMLStreamTransformer transformer, ReadablePipe input, WritablePipe output, XProcRuntime runtime)
			throws TransformerException {
		writePipe(output,
		          SaxonHelper.transform(transformer,
		                                readPipe(input),
		                                runtime.getProcessor().getUnderlyingConfiguration()));
	}
	
	// This class is not used. It was originally intended to be the BaseURIAwareXMLStreamWriter
	// implementation, but it was replaced with SaxonHelper.BaseURIAwareStreamWriterToReceiver.
	
	public static class XMLStreamWriterOverTreeWriter extends TreeWriter implements BaseURIAwareXMLStreamWriter {
		
		// FIXME: change when xml:base attributes are written
		private URI baseURI = null;
		private boolean seenRoot = false;
		
		public XMLStreamWriterOverTreeWriter(Configuration configuration) {
			// FIXME: TreeWriter doesn't need the Processor, it calls getUnderlyingConfiguration
			super(new Processor(configuration));
		}
		
		public XMLStreamWriterOverTreeWriter(Processor processor) {
			super(processor);
		}

		Stack<Boolean> contentStarted = new Stack<>();
		void startContentIfNotStartedYet() {
			if (!contentStarted.isEmpty() && !contentStarted.peek()) {
				startContent();
				contentStarted.pop();
				contentStarted.push(true);
			}
		}

		@Override
		public void close() throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void flush() throws XMLStreamException {
		}

		@Override
		public URI getBaseURI() throws XMLStreamException {
			return baseURI;
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
			if (seenRoot)
				throw new XMLStreamException("Setting base URI not supported after document has started.");
			this.baseURI = baseURI;
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
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
			addAttribute(new net.sf.saxon.s9api.QName(namespaceURI, localName), value);
		}

		@Override
		public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
			addAttribute(new net.sf.saxon.s9api.QName(prefix, namespaceURI, localName), value);
		}

		@Override
		public void writeCData(String data) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeCharacters(String text) throws XMLStreamException {
			startContentIfNotStartedYet();
			addText(text);
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeComment(String data) throws XMLStreamException {
			addComment(data);
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
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
		public void writeEmptyElement(String localName) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeEndDocument() throws XMLStreamException {
			endDocument();
		}

		@Override
		public void writeEndElement() throws XMLStreamException {
			addEndElement();
			contentStarted.pop();
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
			addNamespace(prefix, namespaceURI);
		}

		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
			addPI(target, data);
		}

		@Override
		public void writeStartDocument() throws XMLStreamException {
			startDocument(baseURI);
			seenRoot = true;
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
			startContentIfNotStartedYet();
			addStartElement(new net.sf.saxon.s9api.QName(localName));
			contentStarted.push(false);
		}

		@Override
		public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
			startContentIfNotStartedYet();
			addStartElement(new net.sf.saxon.s9api.QName(namespaceURI, localName));
			contentStarted.push(false);
		}

		@Override
		public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
			startContentIfNotStartedYet();
			addStartElement(new net.sf.saxon.s9api.QName(prefix, namespaceURI, localName));
			contentStarted.push(false);
		}
	}
	
	private XMLCalabashHelper() {
		// no instantiation
	}
}
