package org.daisy.common.transform;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.DelegatingBaseURIAwareXMLStreamReader;

import org.w3c.dom.Node;

/**
 * A supplier of a XDM value that is a sequence of nodes, or an "external" item that can be
 * marshaled to a sequence of nodes.
 *
 * <p>This interface has several sub-interfaces. Instances may implement one or more of them:</p>
 * <ul>
 *   <li>{@link XMLEventReader}</li>
 *   <li>{@link XMLStreamReader}</li>
 *   <li>{@link Iterator}{@code <}{@link Node}{@code >}</li>
 * </ul>
 *
 * {@link #asObject()} will throw a {@link UnsupportedOperationException} unless the value is an
 * external item.
 */
public class XMLInputValue<V> extends InputValue<V> {

	private XMLInputValue<V> backingValue = null;
	protected BaseURIAwareXMLStreamReader streamReader = null;
	private boolean streamReaderSupplied = false;
	private Iterator<Node> nodeIterator = null;
	private boolean nodeIteratorSupplied = false;
	protected final boolean sequence;

	public XMLInputValue(BaseURIAwareXMLStreamReader value) {
		super();
		streamReader = value;
		sequence = true;
	}

	public XMLInputValue(Iterator<? extends Node> value) {
		this(value, true);
	}

	@SuppressWarnings("unchecked") // safe cast
	protected XMLInputValue(Iterator<? extends Node> value, boolean sequence) {
		super();
		nodeIterator = (Iterator<Node>)value;
		this.sequence = sequence;
	}

	protected XMLInputValue(XMLInputValue<V> value, boolean sequence) {
		super(value);
		backingValue = value;
		this.sequence = sequence;
	}

	/**
	 * A stream of XML events as a {@link XMLStreamReader}.
	 */
	public BaseURIAwareXMLStreamReader asXMLStreamReader() throws UnsupportedOperationException, NoSuchElementException {
		if (backingValue != null) {
			if (sequence)
				return backingValue.asXMLStreamReader();
			else
				return ensureSingleItem(backingValue.asXMLStreamReader());
		} else if (streamReader == null)
			throw new UnsupportedOperationException();
		else if (valueSupplied())
			throw new NoSuchElementException();
		else {
			streamReaderSupplied = true;
			if (sequence)
				return streamReader;
			else
				return ensureSingleItem(streamReader);
		}
	}

	/**
	 * A stream of XML events as a {@link XMLEventReader}.
	 */
	public XMLEventReader asXMLEventReader() throws UnsupportedOperationException, NoSuchElementException {
		if (backingValue != null)
			return backingValue.asXMLEventReader();
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * A sequence of nodes as a {@link Iterator}{@code <}{@link Node}{@code >}.
	 */
	public Iterator<Node> asNodeIterator() throws UnsupportedOperationException, NoSuchElementException {
		if (backingValue != null) {
			if (sequence)
				return backingValue.asNodeIterator();
			else
				return ensureSingleItem(backingValue.asNodeIterator());
		} else if (nodeIterator == null)
			throw new UnsupportedOperationException();
		else if (valueSupplied())
			throw new NoSuchElementException();
		else {
			nodeIteratorSupplied = true;
			if (sequence)
				return nodeIterator;
			else
				return ensureSingleItem(nodeIterator);
		}
	}

	@Override
	public Mult<? extends XMLInputValue<V>> mult(int limit) {
		return new Mult<XMLInputValue<V>>() {
			Iterable<Node> nodeCache = cache(
				iteratorOf(
					new Supplier<Node>() {
						Iterator<Node> it = null;
						public Node get() {
							if (it == null) it = asNodeIterator();
							return it.next(); }}),
				limit);
			int supplied = 0;
			public XMLInputValue<V> get() throws NoSuchElementException {
				if (supplied >= limit) {
					nodeCache = null;
					throw new NoSuchElementException();
				}
				supplied++;
				return new XMLInputValue<V>(nodeCache.iterator(), sequence);
			}
		};
	}

	/**
	 * Ensure that the number of items in the sequence is exactly one.
	 */
	public XMLInputValue<V> ensureSingleItem() {
		if (backingValue != null && !sequence)
			return this;
		else
			return new XMLInputValue<V>(this, false);
	}

	@Override
	protected boolean valueSupplied() {
		return super.valueSupplied() || streamReaderSupplied || nodeIteratorSupplied;
	}

	protected static <V> Iterator<V> ensureSingleItem(Iterator<V> iterator) {
		if (!iterator.hasNext())
			throw new TransformerException(new IllegalArgumentException("expected exactly one input item"));
		return new Iterator<V>() {
			public boolean hasNext() {
				return iterator.hasNext();
			}
			public V next() {
				V item = iterator.next();
				if (iterator.hasNext())
					throw new TransformerException(new IllegalArgumentException("expected exactly one input item"));
				return item;
			}
		};
	}

	private static BaseURIAwareXMLStreamReader ensureSingleItem(BaseURIAwareXMLStreamReader reader) {
		try {
			if (!reader.hasNext())
				throw new TransformerException(
					new IllegalArgumentException("expected exactly one input node but got an empty sequence"));
		} catch (XMLStreamException e) {
			throw new TransformerException(e);
		}
		return new DelegatingBaseURIAwareXMLStreamReader() {
			private boolean seenStartDocument = false;
			private String firstNode = null;
			private int elementDepth = 0;
			{
				switch (reader.getEventType()) {
				case START_DOCUMENT:
					seenStartDocument = true;
					break;
				case START_ELEMENT:
					firstNode = "<" + reader.getName().getLocalPart() + "/>";
					elementDepth++;
					break;
				default:
					// the input is assumed to be a sequence of elements or documents
					throw new IllegalArgumentException();
				}
			}
			protected BaseURIAwareXMLStreamReader delegate() {
				return reader;
			}
			@Override
			public int next() throws XMLStreamException, NoSuchElementException {
				int event = super.next();
				switch (event) {
				case START_DOCUMENT:
					if (elementDepth != 0)
						throw new XMLStreamException();
					if (seenStartDocument)
						throw new XMLStreamException();
					seenStartDocument = true;
					break;
				case START_ELEMENT:
					if (firstNode == null)
						firstNode = "<" + reader.getName().getLocalPart() + "/>";
					elementDepth++;
					break;
				case END_ELEMENT:
					if (elementDepth == 0)
						throw new XMLStreamException();
					elementDepth--;
					if (!seenStartDocument && elementDepth == 0)
						if (super.hasNext()) {
							String errMsg = "expected exactly one input node";
							switch (super.next()) {
							case START_ELEMENT:
								String secondNode = "<" + reader.getName().getLocalPart() + "/>";
								errMsg += (" but got (" + firstNode + ", " + secondNode + ", ...)");
								break;
							default:
								errMsg += " but got more than one";
							}
							throw new TransformerException(new IllegalArgumentException(errMsg));
						}
					break;
				case END_DOCUMENT:
					if (elementDepth != 0)
						throw new XMLStreamException();
					if (!seenStartDocument)
						throw new XMLStreamException();
					if (firstNode == null)
						throw new XMLStreamException(); // empty document node, not sure if we should allow this
					if (super.hasNext()) {
						String errMsg = "expected exactly one input node";
						switch (super.next()) {
						case START_DOCUMENT:
							if (super.next() != START_ELEMENT) {
								errMsg += " but got more than one";
								break;
							}
						case START_ELEMENT:
							String secondNode = "<" + reader.getName().getLocalPart() + "/>";
							errMsg += (" but got (" + firstNode + ", " + secondNode + ", ...)");
							break;
						default:
							errMsg += " but got more than one";
						}
						throw new TransformerException(new IllegalArgumentException(errMsg));
					}
					break;
				default:
					if (!seenStartDocument && firstNode == null)
						throw new XMLStreamException(); // not supported for now
				}
				return event;
			}
		};
	}
}
