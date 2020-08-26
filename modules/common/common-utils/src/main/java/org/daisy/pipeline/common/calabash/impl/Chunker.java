package org.daisy.pipeline.common.calabash.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.xmlcalabash.model.RuntimeValue;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.DelegatingXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.getAttributes;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttributes;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeCharacters;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeDocument;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeEvent;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.Mult;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;

// The transformation consists of two passes. In the first pass, the split points are computed based
// on the provided stylesheets. In the second pass, the document is split into chunks. Because the
// XMLInputValue interface does not yet support reading the document more than once as a XMLStreamReader,
// we use the SaxonInputValue interface.

class Chunker extends SingleInSingleOutXMLTransformer {
	
	final RuntimeValue allowBreakBeforeOption;
	final RuntimeValue allowBreakAfterOption;
	final RuntimeValue preferBreakBeforeOption;
	final RuntimeValue preferBreakAfterOption;
	final RuntimeValue alwaysBreakBeforeOption;
	final RuntimeValue alwaysBreakAfterOption;
	final QName partAttribute;
	
	final boolean propagate;
	final int maxChunkSize;
	final Configuration config;
	
	private static final QName _ID = new QName("id");
	private static final QName XML_ID = new QName("http://www.w3.org/XML/1998/namespace", "id", "xml");
	private static final QName D_FILESET = new QName("http://www.daisy.org/ns/pipeline/data", "fileset");
	private static final QName D_FILE = new QName("http://www.daisy.org/ns/pipeline/data", "file");
	private static final QName D_ANCHOR = new QName("http://www.daisy.org/ns/pipeline/data", "anchor");
	private static final QName _HREF = new QName("href");
	private static final QName _ORIGINAL_HREF = new QName("original-href");
	
	Chunker(RuntimeValue allowBreakBeforeOption, RuntimeValue allowBreakAfterOption,
	        RuntimeValue preferBreakBeforeOption, RuntimeValue preferBreakAfterOption,
	        RuntimeValue alwaysBreakBeforeOption, RuntimeValue alwaysBreakAfterOption,
	        QName partAttribute, boolean propagate, int maxChunkSize, Configuration config) {
		this.allowBreakBeforeOption = allowBreakBeforeOption;
		this.allowBreakAfterOption = allowBreakAfterOption;
		this.preferBreakBeforeOption = preferBreakBeforeOption;
		this.preferBreakAfterOption = preferBreakAfterOption;
		this.alwaysBreakBeforeOption = alwaysBreakBeforeOption;
		this.alwaysBreakAfterOption = alwaysBreakAfterOption;
		this.partAttribute = partAttribute;
		this.propagate = propagate;
		this.maxChunkSize = maxChunkSize;
		this.config = config;
	}
	
	private LinkedList<QName> parents;
	private LinkedList<Map<QName,String>> parentAttrs;
	private Path.Builder currentPath;
	private Iterator<BreakPosition> splitPoints;
	private Multimap<Integer,String> idToChunk;
	private BreakPosition nextSplitPoint;

	public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
		if (source == null || result == null)
			throw new IllegalArgumentException();
		if (!(source instanceof SaxonInputValue))
			throw new IllegalArgumentException();
		return () -> transform((SaxonInputValue)source.ensureSingleItem(), result.asXMLStreamWriter());
	}
	
	void transform(SaxonInputValue input, BaseURIAwareXMLStreamWriter output) throws TransformerException {
		Mult<SaxonInputValue> mult = input.mult(2);
		XdmItem item = mult.get().asXdmItemIterator().next();
		if (!(item instanceof XdmNode))
			throw new TransformerException(new IllegalArgumentException());
		XdmNode doc = (XdmNode)item;
		BaseURIAwareXMLStreamReader reader = mult.get().asXMLStreamReader();
		int chunkCount;
		try {
			SortedSet<BreakPosition> collectSplitPoints = new TreeSet<>();
			Map<String,Path> collectIds = new HashMap<>();
			getSplitPoints(doc, collectSplitPoints, collectIds);
			chunkCount = collectSplitPoints.size() + 1;
			idToChunk = ArrayListMultimap.<Integer,String>create();
			int n = 1;
			for (BreakPosition sp : collectSplitPoints) {
				Iterator<Map.Entry<String,Path>> i = collectIds.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry<String,Path> e = i.next();
					if (sp.compareTo(e.getValue()) > 0) {
						idToChunk.put(n, e.getKey());
						i.remove();
					}
				}
				n++;
			}
			for (String id : collectIds.keySet())
				idToChunk.put(n, id);
			splitPoints = collectSplitPoints.iterator();
		} catch (XPathException | SaxonApiException e) {
			throw new TransformerException(e);
		}
		URI inputBase = doc.getBaseURI();
		if (inputBase == null)
			throw new TransformerException(new RuntimeException("source document must have a base URI"));
		if (splitPoints.hasNext()) {
			// first document is the mapping
			try {
				output.setBaseURI(inputBase);
				output.writeStartDocument();
				writeStartElement(output, D_FILESET);
				for (Integer chunk = 1; chunk <= chunkCount; chunk++) {
					writeStartElement(output, D_FILE);
					writeAttribute(output, _HREF, getChunkBaseURI(inputBase, chunk).toASCIIString());
					writeAttribute(output, _ORIGINAL_HREF, inputBase.toASCIIString());
					for (String id : idToChunk.get(chunk)) {
						writeStartElement(output, D_ANCHOR);
						writeAttribute(output, _ID, id);
						output.writeEndElement();
					}
					output.writeEndElement();
				}
				output.writeEndElement();
				output.writeEndDocument();
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
			// then output the chunks
			output = setBaseURI(output, inputBase);
			transform(reader, output);
		} else {
			// first document is the mapping: leave empty
			try {
				output.setBaseURI(inputBase);
				output.writeStartDocument();
				writeStartElement(output, D_FILESET);
				output.writeEndElement();
				output.writeEndDocument();
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
			// pass on the input to the output
			try {
				output.setBaseURI(inputBase);
				writeDocument(output, reader);
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
		}
	}
	
	void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter writer) throws TransformerException {
		nextSplitPoint = null;
		if (splitPoints.hasNext())
			nextSplitPoint = splitPoints.next();
		parents = new LinkedList<>();
		parentAttrs = new LinkedList<>();
		boolean containsSplitPoint = true;
		LinkedList<Boolean> containsSplitPointStack = new LinkedList<>();
		currentPath = new Path.Builder();
		try {
			int event = reader.getEventType();
			while (true)
				try {
					switch (event) {
					case START_ELEMENT:
						containsSplitPointStack.push(containsSplitPoint);
						if (!currentPath.isRoot()) {
							currentPath.nextElement();
							if (containsSplitPoint && isSplitPoint(currentPath, BreakPosition.Side.BEFORE, nextSplitPoint))
								split(writer);
							containsSplitPoint = containsSplitPoint(currentPath, nextSplitPoint);
						}
						writeStartElement(writer, reader);
						writeAttributes(writer, reader);
						if (partAttribute != null && containsSplitPoint)
							writeAttribute(writer, partAttribute, "head");
						currentPath.down();
						if (containsSplitPoint) {
							parents.push(reader.getName());
							parentAttrs.push(getAttributes(reader));
						}
						break;
					case END_ELEMENT:
						if (containsSplitPoint) {
							parents.pop();
							parentAttrs.pop();
						}
						writer.writeEndElement();
						containsSplitPoint = containsSplitPointStack.pop();
						if (containsSplitPoint && isSplitPoint(currentPath, BreakPosition.Side.AFTER, nextSplitPoint))
							split(writer);
						currentPath.up();
						break;
					case CHARACTERS:
						if (currentPath.isElement())
							currentPath.inc();
						if (containsSplitPoint && isSplitPoint(currentPath, BreakPosition.Side.BEFORE, nextSplitPoint))
							split(writer);
						writeCharacters(writer, reader);
						if (containsSplitPoint && isSplitPoint(currentPath, BreakPosition.Side.AFTER, nextSplitPoint))
							split(writer);
						break;
					default:
						writeEvent(writer, reader); }
					event = reader.next();
				} catch (NoSuchElementException e) {
					break;
				}
		} catch (XMLStreamException e) {
			throw new TransformerException(e);
		}
	}
	
	void split(XMLStreamWriter writer) throws XMLStreamException {
		nextSplitPoint = splitPoints.hasNext() ? splitPoints.next() : null;
		for (QName n : parents)
			writer.writeEndElement();
		writer.writeEndDocument();
		writer.writeStartDocument();
		Iterator<QName> elems = parents.descendingIterator();
		Iterator<Map<QName,String>> attrs = parentAttrs.descendingIterator();
		int i = 0;
		while (elems.hasNext()) {
			writeStartElement(writer, elems.next());
			for (Map.Entry<QName,String> attr : attrs.next().entrySet())
				if (!(attr.getKey().equals(_ID) || attr.getKey().equals(XML_ID)))
					writeAttribute(writer, attr);
			if (partAttribute != null) {
				if (containsSplitPoint(currentPath.subPath(i++), nextSplitPoint))
					writeAttribute(writer, partAttribute, "middle");
				else
					writeAttribute(writer, partAttribute, "tail");
			}
		}
	}
	
	static BaseURIAwareXMLStreamWriter setBaseURI(BaseURIAwareXMLStreamWriter output, URI sourceBaseURI) {
		return new DelegatingXMLStreamWriter() {
			int supplied = 0;
			protected BaseURIAwareXMLStreamWriter delegate() {
				return output;
			}
			@Override
			public void writeStartDocument() throws XMLStreamException {
				output.setBaseURI(getChunkBaseURI(sourceBaseURI, ++supplied));
				super.writeStartDocument();
			}
			@Override
			public void writeStartDocument(String version) throws XMLStreamException {
				output.setBaseURI(getChunkBaseURI(sourceBaseURI, ++supplied));
				super.writeStartDocument(version);
			}
			@Override
			public void writeStartDocument(String encoding, String version) throws XMLStreamException {
				output.setBaseURI(getChunkBaseURI(sourceBaseURI, ++supplied));
				super.writeStartDocument(encoding, version);
			}
		};
	}
	
	static URI getChunkBaseURI(URI sourceBaseURI, int chunkNr) {
		return URI.create(sourceBaseURI.toASCIIString().replaceAll("^(.+?)(\\.[^\\.]+)$", "$1-" + chunkNr + "$2"));
	}
	
	final static boolean isSplitPoint(Path elementPath, BreakPosition.Side side, BreakPosition splitPoint) {
		return splitPoint != null && splitPoint.equals(elementPath, side);
	}
	
	final static boolean containsSplitPoint(Path elementPath, BreakPosition splitPoint) {
		if (splitPoint == null)
			return false;
		return elementPath.contains(splitPoint.path);
	}
	
	void getSplitPoints(XdmNode source, SortedSet<BreakPosition> collectSplitPoints, Map<String,Path> collectIds)
			throws XPathException, SaxonApiException {
		SortedSet<BreakOpportunity> opportunities = new TreeSet<>();
		int totalBytes = getBreakOpportunities(source, opportunities, collectIds);
		collectSplitPoints.addAll(computeSplitPoints(opportunities, totalBytes, maxChunkSize * 1000));
	}
	
	static SortedSet<BreakPosition> computeSplitPoints(SortedSet<BreakOpportunity> opportunities, int totalBytes, int maxSize) {
		SortedSet<BreakPosition> splitPoints = new TreeSet<>();
		if (maxSize <= 0) {
			for (BreakOpportunity o : opportunities)
				if (o.weight == BreakOpportunity.Weight.ALWAYS)
					splitPoints.add(o.position);
		} else {
			BreakOpportunity BEGIN = new BreakOpportunity(
				new BreakPosition(Path.ROOT, BreakPosition.Side.BEFORE),
				BreakOpportunity.Weight.ALWAYS,
				0);
			BreakOpportunity END = new BreakOpportunity(
				new BreakPosition(Path.ROOT, BreakPosition.Side.AFTER),
				BreakOpportunity.Weight.ALWAYS,
				totalBytes);
			opportunities.add(BEGIN);
			opportunities.add(END);
			
			// chunk candidates starting from a certain point
			int preferredSize = maxSize * 4 / 5;
			Function<BreakOpportunity,Set<BreakOpportunity>> neighbors = (current) -> {
				Set<BreakOpportunity> result = new TreeSet<>();
				if (current.equals(END))
					return result;
				Iterator<BreakOpportunity> i = opportunities.iterator();
				for (;;)
					try {
						BreakOpportunity n = i.next();
						if (n.equals(current))
							break; }
					catch (NoSuchElementException e) {
						throw new IllegalArgumentException(current+" is not a break opportunity"); }
				while (i.hasNext()) {
					BreakOpportunity n = i.next();
					if (n.bytesBefore - current.bytesBefore <= maxSize) {
						result.add(n);
						if (n.weight == BreakOpportunity.Weight.ALWAYS)
							break; // can not be skipped
					} else {
						if (result.isEmpty())
							result.add(n); // chunk exceeds maximum size
						break;
					}
				}
				return result;
			};
			
			// cost of a single chunk
			// total cost function can be expressed as the sum of the costs of individual chunks
			BiFunction<BreakOpportunity,BreakOpportunity,Float> cost = (begin, end) -> {
				float result = 0;
				result += .2; // the less chunks the better
				switch (end.weight) {
				case ALWAYS:
				case PREFER:
					break;
				case ALLOW:
					result += .2;
				}
				int size = end.bytesBefore - begin.bytesBefore;
				int diff = Math.abs(preferredSize - size);
				if (size > maxSize)
					diff *= 4; // higher penalty
				result += (float)diff / (float)preferredSize;
				return result;
			};
			
			// compute optimal chunking
			List<BreakOpportunity> solution = shortestPath(BEGIN, END, neighbors, cost);
			for (BreakOpportunity o : solution)
				splitPoints.add(o.position);
		}
		return splitPoints;
	}
	
	// Dijkstra's algorithm
	static <T> List<T> shortestPath(T from, T to, Function<T,Set<T>> neighbors, BiFunction<T,T,Float> distance) {
		Map<T,Float> tentativeDistance = new HashMap<>(); // unvisited = tentativeDistance.keySet()
		Set<T> visited = new TreeSet<>();
		Map<T,T> bestPredecessor = new HashMap<>();
		tentativeDistance.put(from, 0f);
		T current = from;
		for (;;) {
			for (T n : neighbors.apply(current)) {
				if (!visited.contains(n)) {
					float distFromCurrent = distance.apply(current, n);
					float distFromBegin = tentativeDistance.get(current) + distFromCurrent;
					if (!tentativeDistance.containsKey(n) || tentativeDistance.get(n) > distFromBegin) {
						tentativeDistance.put(n, distFromBegin);
						bestPredecessor.put(n, current);
					}}}
			visited.add(current);
			tentativeDistance.remove(current);
			try {
				current = Collections.min(
					tentativeDistance.entrySet(),
					Comparator.comparing(Map.Entry::getValue)).getKey();
			} catch (NoSuchElementException e) {
				throw new IllegalArgumentException("neighbors function cannot connect "+from+" and "+to);
			}
			if (current.equals(to)) {
				List<T> path = new ArrayList<>();
				current = to;
				for (;;) {
					current = bestPredecessor.get(current);
					if (current.equals(from))
						break;
					path.add(0, current);
				}
				return path;
			}}
	}
	
	int getBreakOpportunities(XdmNode source, SortedSet<BreakOpportunity> collectBreakOpportunities, Map<String,Path> collectIds)
			throws XPathException, SaxonApiException {
		net.sf.saxon.s9api.QName _ID = new net.sf.saxon.s9api.QName(Chunker._ID);
		net.sf.saxon.s9api.QName XML_ID = new net.sf.saxon.s9api.QName(Chunker.XML_ID);
		Predicate<XdmNode> allowBreakBefore = parseBreakOpportunityOption(allowBreakBeforeOption);
		Predicate<XdmNode> allowBreakAfter = parseBreakOpportunityOption(allowBreakAfterOption);
		Predicate<XdmNode> preferBreakBefore = parseBreakOpportunityOption(preferBreakBeforeOption);
		Predicate<XdmNode> preferBreakAfter = parseBreakOpportunityOption(preferBreakAfterOption);
		Predicate<XdmNode> alwaysBreakBefore = parseBreakOpportunityOption(alwaysBreakBeforeOption);
		Predicate<XdmNode> alwaysBreakAfter = parseBreakOpportunityOption(alwaysBreakAfterOption);
		return new Function<XdmNode,Integer>() {
			Path.Builder currentPath = new Path.Builder();
			public Integer apply(XdmNode node) {
				int bytesSeen = 0;
				if (node.getNodeKind() == XdmNodeKind.DOCUMENT) {
					for (XdmItem i : SaxonHelper.axisIterable(node, Axis.CHILD))
						bytesSeen += apply((XdmNode)i);
				} else if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
					if (!currentPath.isRoot())
						currentPath.nextElement();
					String id = node.getAttributeValue(XML_ID);
					if (id == null) id = node.getAttributeValue(_ID);
					if (id != null)
						collectIds.put(id, currentPath.build());
					if (!currentPath.isRoot())
						if (alwaysBreakBefore.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.BEFORE, BreakOpportunity.Weight.ALWAYS,
							                        bytesSeen, propagate));
						else if (preferBreakBefore.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.BEFORE, BreakOpportunity.Weight.PREFER,
							                        bytesSeen, propagate));
						else if (allowBreakBefore.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.BEFORE, BreakOpportunity.Weight.ALLOW,
							                        bytesSeen, propagate));
					currentPath.down();
					for (XdmItem i : SaxonHelper.axisIterable(node, Axis.CHILD))
						bytesSeen += apply((XdmNode)i);
					currentPath.up();
					if (!currentPath.isRoot())
						if (alwaysBreakAfter.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.AFTER, BreakOpportunity.Weight.ALWAYS,
							                        bytesSeen, propagate));
						else if (preferBreakAfter.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.AFTER, BreakOpportunity.Weight.PREFER,
							                        bytesSeen, propagate));
						else if (allowBreakAfter.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.AFTER, BreakOpportunity.Weight.ALLOW,
							                        bytesSeen, propagate));
				} else if (node.getNodeKind() == XdmNodeKind.TEXT) {
					// FIXME: currently only taking into account character data
					// FIXME: we need to know the encoding in order to correctly compute the size
					bytesSeen += node.getStringValue().getBytes().length;
				}
				return bytesSeen;
			}
		}.apply(source);
	}
	
	Predicate<XdmNode> parseBreakOpportunityOption(RuntimeValue option) throws XPathException {
		if (!option.getString().equals("/*")) {
			XPathExpression matcher = SaxonHelper.compileExpression(option.getString(),
			                                                        option.getNamespaceBindings(),
			                                                        config);
			return n -> n.getNodeKind() == XdmNodeKind.ELEMENT && SaxonHelper.evaluateBoolean(matcher, n);
		} else
			return n -> false;
	}
	
	static <T> void addOptionally(Collection<T> collection, Optional<T> object) {
		if (object.isPresent())
			collection.add(object.get());
	}
	
	static Optional<BreakOpportunity> propagate(XdmNode element, Path.Builder path, BreakPosition.Side side,
	                                            BreakOpportunity.Weight weight, int bytesSeen, boolean propagate) {
		path = path.builder();
		while (!path.isRoot()) {
			if (propagate) {
				if (shouldPropagateBreak(element, side)) {
					element = (XdmNode)element.getParent();
					path.up();
					continue;
				}
				if (side == BreakPosition.Side.AFTER)
					bytesSeen += skipWhiteSpaceNodes(element, side, path);
			}
			return Optional.of(new BreakOpportunity(new BreakPosition(path, side).normalize(), weight, bytesSeen));
		}
		return Optional.empty();
	}
	
	static boolean shouldPropagateBreak(XdmNode element, BreakPosition.Side side) {
		for (XdmItem i : SaxonHelper.axisIterable(element, side == BreakPosition.Side.BEFORE ? Axis.PRECEDING_SIBLING
		                                                                                     : Axis.FOLLOWING_SIBLING)) {
			XdmNode n = (XdmNode)i;
			if (n.getNodeKind() == XdmNodeKind.ELEMENT)
				return false;
			else if (n.getNodeKind() == XdmNodeKind.TEXT && !isWhiteSpaceNode(n))
				return false;
		}
		return true;
	}
	
	static int skipWhiteSpaceNodes(XdmNode element, BreakPosition.Side side, Path.Builder path) {
		int bytesSkipped = 0;
		for (XdmItem i : SaxonHelper.axisIterable(element, side == BreakPosition.Side.BEFORE ? Axis.PRECEDING_SIBLING
		                                                                                     : Axis.FOLLOWING_SIBLING)) {
			XdmNode n = (XdmNode)i;
			if (n.getNodeKind() == XdmNodeKind.ELEMENT) {
				if (side == BreakPosition.Side.AFTER)
					path.inc();
				else
					path.dec();
				return bytesSkipped;
			} else if (n.getNodeKind() == XdmNodeKind.TEXT) {
				if (isWhiteSpaceNode(n))
					bytesSkipped += n.getStringValue().getBytes().length;
				else
					return 0;
			}
		}
		return 0;
	}
	
	static final Pattern WHITESPACE_RE = Pattern.compile("\\s*");
	
	static boolean isWhiteSpaceNode(XdmNode textNode) {
		return isWhiteSpace(textNode.getStringValue());
	}
	
	static boolean isWhiteSpace(String text) {
		return WHITESPACE_RE.matcher(text).matches();
	}
	
	/*
	 * The path of a node in the tree is encoded as a list of integers where each integer
	 * is an index of a child node, starting with the ancestor of the node that is a child
	 * of the root element and ending with the node itself. Indexes are even for elements
	 * (2, 4, ...) and uneven for text nodes (1, 3, ...). This definition matches the
	 * definition of EPUB canonical fragment identifiers (see
	 * http://www.idpf.org/epub/linking/cfi/#sec-epubcfi-def). [x, y, z] corresponds with
	 * /x/y/z in EPUB CFI.
	 */
	static class Path implements Comparable<Path> {
		
		protected final List<Integer> path; // first item is deepest element
		
		final static Path ROOT = new Builder().build();
		
		private Path(List<Integer> path) {
			this.path = path;
		}
		
		public boolean isRoot() {
			return path.isEmpty();
		}
		
		public boolean isElement() {
			return path.isEmpty() || path.get(0) % 2 == 0;
		}
		
		public Builder builder() {
			return new Builder(this);
		}
		
		/**
		 * @return -1 when this node comes before the given node, 1 when this node comes after the
		 *         given node, 0 when the nodes are the same or one is a descendant of the other.
		 */
		@Override
		public int compareTo(Path o) {
			ListIterator<Integer> a = path.listIterator(path.size());
			ListIterator<Integer> b = o.path.listIterator(o.path.size());
			while (a.hasPrevious() && b.hasPrevious()) {
				int c = a.previous().compareTo(b.previous());
				if (c != 0) return c;
			}
			return 0;
		}
		
		public boolean contains(Path o) {
			if (compareTo(o) != 0)
				return false;
			return o.path.size() > this.path.size();
		}
		
		/** @param depth 0 means root, 1 means ancestor node that is one level below the root element, ... */
		public Path subPath(int depth) {
			if (depth < 0)
				throw new IllegalArgumentException();
			else if (depth == 0)
				return ROOT;
			else
				return new Path(path.subList(path.size() - depth, path.size()));
		}
		
		static class Builder extends Path {
			
			private final LinkedList<Integer> mutablePath;
			
			public Builder() {
				super(new LinkedList<>());
				mutablePath = (LinkedList<Integer>)path;
			}
			
			private Builder(Path from) {
				this();
				for (Integer step : from.path)
					mutablePath.add(step);
			}
			
			public Builder down() {
				mutablePath.push(0);
				return this;
			}
			
			public Builder up() {
				mutablePath.pop();
				return this;
			}
			
			public Builder inc() {
				mutablePath.push(mutablePath.pop() + 1);
				return this;
			}
			
			public Builder dec() {
				mutablePath.push(mutablePath.pop() - 1);
				return this;
			}
			
			public Builder nextElement() {
				int step = mutablePath.pop() + 1;
				if (step % 2 == 1)
					step++;
				mutablePath.push(step);
				return this;
			}
			
			public Path build() {
				List<Integer> copy = new LinkedList<>();
				for (Integer step : path)
					copy.add(step);
				return new Path(copy);
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + path.hashCode();
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Path other = (Path)obj;
			return this.path.equals(other.path);
		}
		
		@Override
		public String toString() {
			if (isRoot())
				return "/";
			StringBuilder b = new StringBuilder();
			for (Integer i : path)
				b.insert(0, "/" + i);
			return b.toString();
		}
		
		// for testing
		static Path parse(String string) {
			LinkedList<Integer> path = new LinkedList<>();
			if ("/".equals(string))
				return ROOT;
			if (!string.matches("(/(0|[1-9][0-9]*))+"))
				throw new IllegalArgumentException();
			String[] steps = string.split("/");
			for (int i = 1; i < steps.length; i++)
				path.push(Integer.parseInt(steps[i]));
			return new Path(path);
		}
	}
	
	static class BreakPosition implements Comparable<BreakPosition> {
		
		enum Side { BEFORE, AFTER }
		final Path path;
		final Side side;
		
		BreakPosition(Path path, Side side) {
			if (path == null || side == null)
				throw new NullPointerException();
			this.path = path;
			this.side = side;
		}
		
		// normalize to BEFORE
		BreakPosition normalize() {
			if (side == Side.BEFORE)
				return this;
			else
				return new BreakPosition(path.builder().inc(), Side.BEFORE);
		}
		
		@Override
		public int compareTo(BreakPosition o) {
			int c = this.path.compareTo(o.path);
			if (c != 0)
				return c;
			if (this.path.contains(o.path))
				return this.side == Side.BEFORE ? -1 : 1;
			else if (o.path.contains(this.path))
				return o.side == Side.BEFORE ? 1 : -1;
			else
				return side.compareTo(o.side);
		}
		
		/**
		 * @param path The position of the node
		 * @return -1 when the split point comes before the node, 0 when the split point lies inside
		 *         the node, 1 when the split point comes after the node.
		 */
		public int compareTo(Path path) {
			int c = this.path.compareTo(path);
			if (c != 0)
				return c;
			if (this.path.contains(path))
				return 0;
			else if (this.side == Side.BEFORE)
				return -1;
			else
				return 1;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + path.hashCode();
			result = prime * result + side.hashCode();
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BreakPosition other = (BreakPosition)obj;
			return equals(other.path, other.side);
		}
		
		public boolean equals(Path path, Side side) {
			if (path == null)
				return false;
			if (this.side != side)
				return false;
			if (!this.path.equals(path))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return side + " " + path;
		}
	}
	
	static class BreakOpportunity implements Comparable<BreakOpportunity> {
		
		enum Weight { ALWAYS, PREFER, ALLOW }
		final BreakPosition position;
		final Weight weight;
		final int bytesBefore;
		
		BreakOpportunity(BreakPosition position, Weight weight, int bytesBefore) {
			this.position = position;
			this.weight = weight;
			this.bytesBefore = bytesBefore;
		}
		
		@Override
		public int compareTo(BreakOpportunity o) {
			int c = this.position.compareTo(o.position);
			if (c != 0)
				return c;
			return this.weight.compareTo(o.weight);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + bytesBefore;
			result = prime * result + position.hashCode();
			result = prime * result + weight.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BreakOpportunity other = (BreakOpportunity)obj;
			if (bytesBefore != other.bytesBefore)
				return false;
			if (!position.equals(other.position))
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return weight + " " + position;
		}
	}
}
