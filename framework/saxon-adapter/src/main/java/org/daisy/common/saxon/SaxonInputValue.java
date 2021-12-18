package org.daisy.common.saxon;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import javax.xml.stream.XMLStreamException;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import com.saxonica.xqj.pull.PullFromIterator;
import com.saxonica.xqj.pull.PullToStax;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.DelegatingXMLStreamReader;
import org.daisy.common.transform.Mult;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;

import org.w3c.dom.Node;

public class SaxonInputValue extends XMLInputValue<Void> {

	private SaxonInputValue backingValue = null;
	private XdmValue xdmValue = null;
	private final Iterator<XdmItem> xdmItemIterator;
	private boolean xdmItemIteratorSupplied = false;

	public SaxonInputValue(XdmValue value) {
		this(value,  true);
	}

	private SaxonInputValue(XdmValue value, boolean sequence) {
		this(value.iterator(), sequence);
		xdmValue = value;
	}

	public SaxonInputValue(NodeInfo value) {
		this(new XdmNode(value));
	}

	public SaxonInputValue(Iterator<? extends XdmItem> value) {
		this(value, true);
	}

	@SuppressWarnings("unchecked") // safe cast
	private SaxonInputValue(Iterator<? extends XdmItem> value, boolean sequence) {
		super(Iterators.transform(
				value,
				n -> {
					if (n instanceof XdmNode)
						// FIXME: why return documents? couldn't we also use NodeOverNodeInfo?
						return DocumentOverNodeInfo.wrap(((XdmNode)n).getUnderlyingNode());
					else
						throw new TransformerException(new IllegalArgumentException("expected a node"));
				}),
		      sequence);
		streamReader = concat(
			Iterators.transform(
				value,
				propagateCE(
					n -> {
						if (n instanceof XdmNode) {
							NodeInfo node = ((XdmNode)n).getUnderlyingNode();
							// iterate() throws XPathException when evaluating sequence results in dynamic error
							// (should not happen with a node?)
							PullFromIterator provider = new PullFromIterator(node.iterate());
							provider.setPipelineConfiguration(new PipelineConfiguration(node.getConfiguration()));
							return new BaseURIAwarePullToStax(
								provider,
								node.getBaseURI() == null ? null : URI.create(node.getBaseURI()));
						} else
							throw new IllegalArgumentException("expected a node");
					},
					TransformerException::wrap)));
		xdmItemIterator = (Iterator<XdmItem>)value;
	}

	protected SaxonInputValue(SaxonInputValue value, boolean sequence) {
		super(value, sequence);
		backingValue = value;
		xdmItemIterator = null; // will not be accessed
	}

	public Iterator<XdmItem> asXdmItemIterator() throws NoSuchElementException {
		if (backingValue != null) {
			if (sequence)
				return backingValue.asXdmItemIterator();
			else
				return ensureSingleItem(backingValue.asXdmItemIterator());
		} else if (valueSupplied())
			throw new NoSuchElementException();
		else {
			xdmItemIteratorSupplied = true;
			return xdmItemIterator;
		}
	}

	/**
	 * <p>The returned {@link BaseURIAwareXMLStreamReader} will throw a {@link TransformerException} that wraps:</p>
	 * <ul>
	 *   <li>a {@link IllegalArgumentException} when an item is encountered that is not a node.</li>
	 * </ul>
	 */
	@Override
	public BaseURIAwareXMLStreamReader asXMLStreamReader() throws NoSuchElementException {
		return super.asXMLStreamReader();
	}

	/**
	 * <p>The returned {@link Iterator} will throw a {@link TransformerException} that wraps:</p>
	 * <ul>
	 *   <li>a {@link IllegalArgumentException} when an item is encountered that is not a node.</li>
	 * </ul>
	 */
	@Override
	public Iterator<Node> asNodeIterator() throws NoSuchElementException {
		return super.asNodeIterator();
	}

	@Override
	public Mult<SaxonInputValue> mult(int limit) {
		if (xdmValue != null && valueSupplied()) {
			// to prevent that mult() is called multiple times
			xdmItemIteratorSupplied = true;
			return () -> this;
		}
		return new Mult<SaxonInputValue>() {
			Iterable<XdmItem> xdmItemCache = xdmValue == null
				? cache(
					iteratorOf(
						new Supplier<XdmItem>() {
							Iterator<XdmItem> it = null;
							public XdmItem get() {
								if (it == null) it = asXdmItemIterator();
								return it.next(); }}),
					limit)
				: null;
			int supplied = 0;
			public SaxonInputValue get() throws NoSuchElementException {
				if (supplied >= limit) {
					xdmItemCache = null;
					throw new NoSuchElementException();
				}
				supplied++;
				if (xdmValue != null)
					return new SaxonInputValue(xdmValue, sequence);
				else
					return new SaxonInputValue(xdmItemCache.iterator(), sequence);
			}
		};
	}

	@Override
	public SaxonInputValue ensureSingleItem() {
		if (backingValue != null && !sequence)
			return this;
		else
			return new SaxonInputValue(this, false);
	}

	@Override
	protected boolean valueSupplied() {
		return super.valueSupplied() || xdmItemIteratorSupplied;
	}

	private static class BaseURIAwarePullToStax extends PullToStax implements BaseURIAwareXMLStreamReader {

		// FIXME: change when xml:base attributes are encountered
		private final URI baseURI;

		public BaseURIAwarePullToStax(PullProvider provider, URI baseURI) {
			super(provider);
			this.baseURI = baseURI;
		}

		public URI getBaseURI() throws XMLStreamException {
			return baseURI;
		}
	}

	private static BaseURIAwareXMLStreamReader concat(Iterator<BaseURIAwareXMLStreamReader> readers) {
		if (!readers.hasNext())
			return null;
		return new DelegatingXMLStreamReader() {
			private BaseURIAwareXMLStreamReader reader = null;
			protected BaseURIAwareXMLStreamReader delegate() {
				if (reader == null)
					reader = readers.next();
				return reader;
			}
			@Override
			public boolean hasNext() throws XMLStreamException {
				return (reader != null && reader.hasNext()) || readers.hasNext();
			}
			@Override
			public int next() throws XMLStreamException, NoSuchElementException {
				if (reader != null && reader.hasNext())
					return reader.next();
				else if (readers.hasNext()) {
					reader = readers.next();
					return reader.getEventType();
				} else
					throw new NoSuchElementException();
			}
		};
	}

	@FunctionalInterface
	private static interface ThrowingFunction<T,R> extends Function<T,R> {
		@Override
		default R apply(T t) {
			try {
				return applyThrows(t);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		R applyThrows(T t) throws Throwable;
	}

	private static <T,R,E extends RuntimeException> Function<T,R> propagateCE(ThrowingFunction<T,R> f, Function<Throwable,E> newEx) {
		return new Function<T,R>() {
			public R apply(T t) throws E {
				try {
					return f.applyThrows(t);
				} catch (RuntimeException e) {
					throw e;
				} catch (Throwable e) {
					throw newEx.apply(e);
				}
			}
		};
	}
}
