package org.daisy.common.xproc.calabash.steps;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.Stack;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Iterators;

import com.xmlcalabash.model.RuntimeValue;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.daisy.common.saxon.NodeToXMLStreamTransformer;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.getAttributes;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeCharacters;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeDocument;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeEvent;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.transform.TransformerException;

// The transformation consists of two passes. In the first pass, the split points are computed based
// on the provided stylesheets. In the second pass, the document is split into chunks. Because the
// XMLStreamToXMLStreamTransformer interface does not yet support reading the input document more
// than once, we use the Saxon specific NodeToXMLStreamTransformer interface.
class Chunker implements NodeToXMLStreamTransformer {
	
	final RuntimeValue allowBreakBeforeOption;
	final RuntimeValue allowBreakAfterOption;
	final RuntimeValue preferBreakBeforeOption;
	final RuntimeValue preferBreakAfterOption;
	final RuntimeValue alwaysBreakBeforeOption;
	final RuntimeValue alwaysBreakAfterOption;
	
	final int maxChunkSize;
	final QName linkAttributeName;
	final Configuration config;
	
	private static final QName _ID = new QName("id");
	private static final QName XML_ID = new QName("http://www.w3.org/XML/1998/namespace", "id", "xml");
	
	Chunker(RuntimeValue allowBreakBeforeOption, RuntimeValue allowBreakAfterOption,
	        RuntimeValue preferBreakBeforeOption, RuntimeValue preferBreakAfterOption,
	        RuntimeValue alwaysBreakBeforeOption, RuntimeValue alwaysBreakAfterOption,
	        int maxChunkSize, QName linkAttributeName, Configuration config) {
		this.allowBreakBeforeOption = allowBreakBeforeOption;
		this.allowBreakAfterOption = allowBreakAfterOption;
		this.preferBreakBeforeOption = preferBreakBeforeOption;
		this.preferBreakAfterOption = preferBreakAfterOption;
		this.alwaysBreakBeforeOption = alwaysBreakBeforeOption;
		this.alwaysBreakAfterOption = alwaysBreakAfterOption;
		this.maxChunkSize = maxChunkSize;
		this.linkAttributeName = linkAttributeName;
		this.config = config;
	}
	
	private Stack<QName> parents;
	private Stack<Map<QName,String>> parentAttrs;
	private Path.Builder currentPath;
	private Iterator<BreakPosition> splitPoints;
	private Map<String,Integer> idToChunk;
	private BreakPosition nextSplitPoint;
	
	public void transform(Iterator<XdmNode> input, Supplier<BaseURIAwareXMLStreamWriter> output) throws TransformerException {
		XdmNode doc = Iterators.getOnlyElement(input);
		BaseURIAwareXMLStreamReader reader;
		try {
			reader = SaxonHelper.nodeReader(doc, config);
			SortedSet<BreakPosition> collectSplitPoints = new TreeSet<>();
			Map<String,Path> collectIds = new HashMap<>();
			getSplitPoints(doc, collectSplitPoints, collectIds);
			idToChunk = new HashMap<String,Integer>();
			int n = 1;
			for (BreakPosition sp : collectSplitPoints) {
				Iterator<Map.Entry<String,Path>> i = collectIds.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry<String,Path> e = i.next();
					if (sp.compareTo(e.getValue()) > 0) {
						idToChunk.put(e.getKey(), n);
						i.remove();
					}
				}
				n++;
			}
			for (String id : collectIds.keySet())
				idToChunk.put(id, n);
			splitPoints = collectSplitPoints.iterator();
		} catch (XPathException | SaxonApiException e) {
			throw new TransformerException(e);
		}
		if (splitPoints.hasNext()) {
			if (doc.getBaseURI() != null)
				output = setBaseURI(output, doc.getBaseURI());
			transform(reader, output);
		} else
			try {
				BaseURIAwareXMLStreamWriter writer = output.get();
				if (doc.getBaseURI() != null)
					writer.setBaseURI(doc.getBaseURI());
				writeDocument(writer, reader);
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
	}
	
	void transform(BaseURIAwareXMLStreamReader reader, Supplier<BaseURIAwareXMLStreamWriter> writers) throws TransformerException {
		nextSplitPoint = null;
		if (splitPoints.hasNext())
			nextSplitPoint = splitPoints.next();
		parents = new Stack<QName>();
		parentAttrs = new Stack<Map<QName,String>>();
		boolean containsSplitPoint = true;
		Stack<Boolean> containsSplitPointStack = new Stack<Boolean>();
		currentPath = new Path.Builder();
		BaseURIAwareXMLStreamWriter writer = writers.get();
		try {
			URI sourceBaseURI = reader.getBaseURI();
			writer.writeStartDocument();
		  loop: while (true)
				try {
					int event = reader.next();
					switch (event) {
					case START_ELEMENT:
						containsSplitPointStack.push(containsSplitPoint);
						if (!currentPath.isRoot()) {
							currentPath.nextElement();
							if (containsSplitPoint && isSplitPoint(currentPath, BreakPosition.Side.BEFORE, nextSplitPoint))
								split(writer, writer = writers.get());
							containsSplitPoint = containsSplitPoint(currentPath, nextSplitPoint);
						}
						writeStartElement(writer, reader);
						for (int i = 0; i < reader.getAttributeCount(); i++) {
							QName attr = reader.getAttributeName(i);
							String val = reader.getAttributeValue(i);
							if (sourceBaseURI != null)
								if (attr.equals(linkAttributeName) && val.startsWith("#")) {
									String id = val.substring(1);
									if (idToChunk.containsKey(id)) {
										URI currentBaseURI = writer.getBaseURI();
										URI targetBaseURI = getChunkBaseURI(sourceBaseURI, idToChunk.get(id));
										if (!currentBaseURI.equals(targetBaseURI))
											val = relativize(currentBaseURI, targetBaseURI).toASCIIString() + val; }}
							writeAttribute(writer, attr, val);
						}
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
							split(writer, writer = writers.get());
						currentPath.up();
						break;
					case CHARACTERS:
						if (currentPath.isElement())
							currentPath.inc();
						if (containsSplitPoint && isSplitPoint(currentPath, BreakPosition.Side.BEFORE, nextSplitPoint))
							split(writer, writer = writers.get());
						writeCharacters(writer, reader);
						if (containsSplitPoint && isSplitPoint(currentPath, BreakPosition.Side.AFTER, nextSplitPoint))
							split(writer, writer = writers.get());
						break;
					case START_DOCUMENT:
						break;
					case END_DOCUMENT:
						break loop;
					default:
						writeEvent(writer, event, reader);
					}
				} catch (NoSuchElementException e) {
					break;
				}
			writer.writeEndDocument();
		} catch (XMLStreamException e) {
			throw new TransformerException(e);
		}
	}
	
	void split(XMLStreamWriter writer, XMLStreamWriter newWriter) throws XMLStreamException {
		for (int i = parents.size(); i > 0; i--)
			writer.writeEndElement();
		writer.writeEndDocument();
		newWriter.writeStartDocument();
		for (int i = 0; i < parents.size(); i++) {
			writeStartElement(newWriter, parents.get(i));
			for (Map.Entry<QName,String> attr : parentAttrs.get(i).entrySet())
				if (!(attr.getKey().equals(_ID) || attr.getKey().equals(XML_ID)))
					writeAttribute(newWriter, attr);
		}
		nextSplitPoint = splitPoints.hasNext() ? splitPoints.next() : null;
	}
	
	static Supplier<BaseURIAwareXMLStreamWriter> setBaseURI(Supplier<BaseURIAwareXMLStreamWriter> output, URI sourceBaseURI) {
		return new Supplier<BaseURIAwareXMLStreamWriter>() {
			int supplied = 0;
			public BaseURIAwareXMLStreamWriter get() throws TransformerException {
				BaseURIAwareXMLStreamWriter writer = output.get();
				try {
					writer.setBaseURI(getChunkBaseURI(sourceBaseURI, ++supplied));
				} catch (XMLStreamException e) {
					throw new TransformerException(e);
				}
				return writer;
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
		Predicate<XdmNode> allowBreakBefore = parseOption(allowBreakBeforeOption);
		Predicate<XdmNode> allowBreakAfter = parseOption(allowBreakAfterOption);
		Predicate<XdmNode> preferBreakBefore = parseOption(preferBreakBeforeOption);
		Predicate<XdmNode> preferBreakAfter = parseOption(preferBreakAfterOption);
		Predicate<XdmNode> alwaysBreakBefore = parseOption(alwaysBreakBeforeOption);
		Predicate<XdmNode> alwaysBreakAfter = parseOption(alwaysBreakAfterOption);
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
							              propagate(node, currentPath, BreakPosition.Side.BEFORE, BreakOpportunity.Weight.ALWAYS, bytesSeen));
						else if (preferBreakBefore.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.BEFORE, BreakOpportunity.Weight.PREFER, bytesSeen));
						else if (allowBreakBefore.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.BEFORE, BreakOpportunity.Weight.ALLOW, bytesSeen));
					currentPath.down();
					for (XdmItem i : SaxonHelper.axisIterable(node, Axis.CHILD))
						bytesSeen += apply((XdmNode)i);
					currentPath.up();
					if (!currentPath.isRoot())
						if (alwaysBreakAfter.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.AFTER, BreakOpportunity.Weight.ALWAYS, bytesSeen));
						else if (preferBreakAfter.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.AFTER, BreakOpportunity.Weight.PREFER, bytesSeen));
						else if (allowBreakAfter.test(node))
							addOptionally(collectBreakOpportunities,
							              propagate(node, currentPath, BreakPosition.Side.AFTER, BreakOpportunity.Weight.ALLOW, bytesSeen));
				} else if (node.getNodeKind() == XdmNodeKind.TEXT) {
					// FIXME: currently only taking into account character data
					// FIXME: we need to know the encoding in order to correctly compute the size
					bytesSeen += node.getStringValue().getBytes().length;
				}
				return bytesSeen;
			}
		}.apply(source);
	}
	
	Predicate<XdmNode> parseOption(RuntimeValue option) throws XPathException {
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
	                                            BreakOpportunity.Weight weight, int bytesSeen) {
		path = path.builder();
		while (!path.isRoot()) {
			if (shouldPropagateBreak(element, side)) {
				element = (XdmNode)element.getParent();
				path.up();
				continue;
			}
			if (side == BreakPosition.Side.AFTER)
				bytesSeen += skipWhiteSpaceNodes(element, side, path);
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
	
	// FIXME: move to some common utility package
	static URI relativize(URI base, URI child) {
		try {
			if (base.isOpaque() || child.isOpaque()
			    || !Optional.ofNullable(base.getScheme()).orElse("").equalsIgnoreCase(Optional.ofNullable(child.getScheme()).orElse(""))
			    || !Optional.ofNullable(base.getAuthority()).equals(Optional.ofNullable(child.getAuthority())))
				return child;
			else {
				String bp = base.normalize().getPath();
				String cp = child.normalize().getPath();
				String relativizedPath;
				if (cp.startsWith("/")) {
					String[] bpSegments = bp.split("/", -1);
					String[] cpSegments = cp.split("/", -1);
					int i = bpSegments.length - 1;
					int j = 0;
					while (i > 0) {
						if (bpSegments[j].equals(cpSegments[j])) {
							i--;
							j++; }
						else
							break; }
					relativizedPath = "";
					while (i > 0) {
						relativizedPath += "../";
						i--; }
					while (j < cpSegments.length) {
						relativizedPath += cpSegments[j] + "/";
						j++; }
					relativizedPath = relativizedPath.substring(0, relativizedPath.length() - 1); }
				else
					relativizedPath = cp;
				if (relativizedPath.isEmpty())
					relativizedPath = "./";
				return new URI(null, null, relativizedPath, child.getQuery(), child.getFragment()); }
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
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
		
		protected final Stack<Integer> path;
		
		final static Path ROOT = new Builder().build();
		
		private Path(Stack<Integer> path) {
			this.path = path;
		}
		
		public boolean isRoot() {
			return path.isEmpty();
		}
		
		public boolean isElement() {
			return path.isEmpty() || path.peek() % 2 == 0;
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
			int minsize = Math.min(this.path.size(), o.path.size());
			for (int i = 0; i < minsize; i++) {
				int c = this.path.get(i).compareTo(o.path.get(i));
				if (c != 0)
					return c;
			}
			return 0;
		}
		
		public boolean contains(Path o) {
			if (compareTo(o) != 0)
				return false;
			return o.path.size() > this.path.size();
		}
		
		static class Builder extends Path {
			
			public Builder() {
				super(new Stack<>());
			}
			
			private Builder(Path from) {
				super(new Stack<Integer>());
				for (Integer step : from.path)
					path.add(step);
			}
			
			public Builder down() {
				path.add(0);
				return this;
			}
			
			public Builder up() {
				path.pop();
				return this;
			}
			
			public Builder inc() {
				path.add(path.pop() + 1);
				return this;
			}
			
			public Builder dec() {
				path.add(path.pop() - 1);
				return this;
			}
			
			public Builder nextElement() {
				int step = path.pop() + 1;
				if (step % 2 == 1)
					step++;
				path.add(step);
				return this;
			}
			
			public Path build() {
				Stack<Integer> copy = new Stack<Integer>();
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
				b.append('/').append(i);
			return b.toString();
		}
		
		// for testing
		static Path parse(String string) {
			Stack<Integer> path = new Stack<>();
			if ("/".equals(string))
				return ROOT;
			if (!string.matches("(/(0|[1-9][0-9]*))+"))
				throw new IllegalArgumentException();
			String[] steps = string.split("/");
			for (int i = 1; i < steps.length; i++)
				path.add(Integer.parseInt(steps[i]));
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
