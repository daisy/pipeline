package org.daisy.common.transform;

import java.util.function.Consumer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;

import org.w3c.dom.Node;

/**
 * A consumer of a XDM value that is a sequence of nodes, or an "external" item that can be
 * unmarshaled from a sequence of nodes.
 *
 * <p>This interface has several sub-interfaces. Instances may implement one or more of them:</p>
 * <ul>
 *   <li>{@link XMLEventWriter}</li>
 *   <li>{@link XMLStreamWriter}</li>
 *   <li>{@link Consumer}{@code <}{@link Node}{@code >}</li>
 * </ul>
 */
public class XMLOutputValue<V> extends OutputValue<V> {

	private XMLOutputValue<V> backingValue = null;
	private BaseURIAwareXMLStreamWriter streamWriter = null;
	private Consumer<Node> nodeConsumer = null;

	public XMLOutputValue(BaseURIAwareXMLStreamWriter value) {
		streamWriter = value;
	}

	public XMLOutputValue(Consumer<Node> value) {
		nodeConsumer = value;
	}

	protected XMLOutputValue() {
	}

	protected XMLOutputValue(XMLOutputValue<V> value) {
		backingValue = value;
	}

	/**
	 * A sequence of XML events, as a {@link XMLStreamWriter}.
	 */
	public BaseURIAwareXMLStreamWriter asXMLStreamWriter() throws UnsupportedOperationException {
		if (backingValue != null)
			return backingValue.asXMLStreamWriter();
		else if (streamWriter != null)
			return streamWriter;
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * The purpose of this method is to be overridden by subclasses, e.g. to implement a lazy output.
	 */
	public void writeXMLStream(Consumer<BaseURIAwareXMLStreamWriter> stream) throws UnsupportedOperationException {
		stream.accept(asXMLStreamWriter());
	}

	/**
	 * A sequence of XML events, as a {@link XMLEventWriter}.
	 */
	public XMLEventWriter asXMLEventWriter() throws UnsupportedOperationException {
		if (backingValue != null)
			return backingValue.asXMLEventWriter();
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * A sequence of nodes, as a {@link Consumer}{@code <}{@link Node}{@code >}.
	 */
	public Consumer<Node> asNodeConsumer() throws UnsupportedOperationException {
		if (backingValue != null)
			return backingValue.asNodeConsumer();
		else if (nodeConsumer != null)
			return nodeConsumer;
		else
			throw new UnsupportedOperationException();
	}
}
