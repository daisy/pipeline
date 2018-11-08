package org.daisy.common.saxon;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.net.URI;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.stream.XMLStreamException;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import com.saxonica.xqj.pull.PullFromIterator;
import com.saxonica.xqj.pull.PullToStax;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper;
import org.daisy.common.stax.XMLStreamWriterHelper.XMLStreamWritable;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.DOMToXMLStreamTransformer;
import org.daisy.common.transform.XMLStreamToXMLStreamTransformer;

import org.w3c.dom.Document;

public final class SaxonHelper {
	
	public static javax.xml.namespace.QName jaxpQName(QName name) {
		String prefix = name.getPrefix();
		String ns = name.getNamespaceURI();
		String localPart = name.getLocalName();
		if (prefix != null)
			return new javax.xml.namespace.QName(ns, localPart, prefix);
		else
			return new javax.xml.namespace.QName(ns, localPart);
	}
	
	public static Iterable<XdmItem> axisIterable(XdmNode node, Axis axis) {
		return new Iterable<XdmItem>() {
			public Iterator<XdmItem> iterator() {
				return node.axisIterator(axis);
			}
		};
	}
	
	public static XPathExpression compileExpression(String expression, Hashtable<String,String> namespaceBindings, Configuration configuration)
			throws XPathException {
		XPathEvaluator xpathEvaluator = new XPathEvaluator(configuration);
		xpathEvaluator.getStaticContext().setNamespaceResolver(new MatchingNamespaceResolver(namespaceBindings));
		return xpathEvaluator.createPattern(expression);
	}
	
	public static boolean evaluateBoolean(XPathExpression expression, XdmNode contextNode) {
		try {
			XPathDynamicContext context = expression.createDynamicContext(contextNode.getUnderlyingNode());
			return expression.effectiveBooleanValue(context);
		} catch (XPathException e) {
			return false;
		}
	}
	
	public static XMLStreamWritable<XdmNode> nodeWriter(Configuration configuration) throws SaxonApiException, XPathException {
		XdmDestination destination = new XdmDestination();
		Receiver receiver = new NamespaceReducer(destination.getReceiver(configuration));
		receiver.open();
		BaseURIAwareXMLStreamWriter writer = new BaseURIAwareStreamWriterToReceiver(receiver);
		return new XMLStreamWritable<XdmNode>() {
			public BaseURIAwareXMLStreamWriter getWriter() {
				return writer;
			}
			public XdmNode doneWriting() throws TransformerException {
				try {
					receiver.close();
					return destination.getXdmNode();
				} catch (XPathException e) {
					throw new TransformerException(e);
				}
			}
		};
	}
	
	public static BaseURIAwareXMLStreamReader nodeReader(XdmNode node, Configuration configuration) throws XPathException {
		return nodeReader(node.getUnderlyingNode(), configuration);
	}
	
	public static BaseURIAwareXMLStreamReader nodeReader(NodeInfo node, Configuration configuration) throws XPathException {
		PullFromIterator provider= new PullFromIterator(node.iterate());
		provider.setPipelineConfiguration(new PipelineConfiguration(configuration));
		return new BaseURIAwarePullToStax(
			provider,
			node.getBaseURI() == null ? null : URI.create(node.getBaseURI()));
	}
	
	public static Iterator<XdmNode> transform(XMLStreamToXMLStreamTransformer transformer, Iterator<NodeInfo> input, Configuration config)
			throws TransformerException {
		Iterator<BaseURIAwareXMLStreamReader> readers = Iterators.transform(input, propagateCE(doc -> nodeReader(doc, config), TransformerException::new));
		Consumer<Supplier<BaseURIAwareXMLStreamWriter>> transform = writers -> transformer.transform(readers, writers);
		Supplier<XMLStreamWritable<XdmNode>> writables = propagateCE(() -> nodeWriter(config), TransformerException::new);
		return XMLStreamWriterHelper.collect(transform, writables).iterator();
	}
	
	public static Iterator<XdmNode> transform(DOMToXMLStreamTransformer transformer, Iterator<NodeInfo> input, Configuration config)
			throws TransformerException {
		Iterator<Document> readers = Iterators.transform(input, doc -> (Document)DocumentOverNodeInfo.wrap(doc));
		Consumer<Supplier<BaseURIAwareXMLStreamWriter>> transform = writers -> transformer.transform(readers, writers);
		Supplier<XMLStreamWritable<XdmNode>> writables = propagateCE(() -> nodeWriter(config), TransformerException::new);
		return XMLStreamWriterHelper.collect(transform, writables).iterator();
	}
	
	public static Iterator<XdmNode> transform(NodeToXMLStreamTransformer transformer, Iterator<XdmNode> input, Configuration config)
			throws TransformerException {
		Consumer<Supplier<BaseURIAwareXMLStreamWriter>> transform = writers -> transformer.transform(input, writers);
		Supplier<XMLStreamWritable<XdmNode>> writables = propagateCE(() -> nodeWriter(config), TransformerException::new);
		return XMLStreamWriterHelper.collect(transform, writables).iterator();
	}
	
	// copied from com.xmlcalabash.util.ProcessMatch
	public static class MatchingNamespaceResolver implements NamespaceResolver {
		
		private Hashtable<String,String> ns = new Hashtable<String,String>();
		
		public MatchingNamespaceResolver(Hashtable<String,String> bindings) {
			ns = bindings;
		}
		
		public String getURIForPrefix(String prefix, boolean useDefault) {
			if ("".equals(prefix) && !useDefault) {
				return "";
			}
			return ns.get(prefix);
		}
		
		public Iterator<String> iteratePrefixes() {
			Vector<String> p = new Vector<String> ();
			for (String pfx : ns.keySet()) {
				p.add(pfx);
			}
			return p.iterator();
		}
	}
	
	public static class BaseURIAwarePullToStax extends PullToStax implements BaseURIAwareXMLStreamReader {
		
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
	
	public static class BaseURIAwareStreamWriterToReceiver extends StreamWriterToReceiver implements BaseURIAwareXMLStreamWriter {
		
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
	
	@FunctionalInterface
	private static interface ThrowingSupplier<T> extends Supplier<T> {
		@Override
		default T get() {
			try {
				return getThrows();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		T getThrows() throws Throwable;
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
	
	private static <T,E extends RuntimeException> Supplier<T> propagateCE(ThrowingSupplier<T> f, Function<Throwable,E> newEx) {
		return new Supplier<T>() {
			public T get() throws E {
				try {
					return f.getThrows();
				} catch (RuntimeException e) {
					throw e;
				} catch (Throwable e) {
					throw newEx.apply(e);
				}
			}
		};
	}
	
	private SaxonHelper() {
		// no instantiation
	}
}
