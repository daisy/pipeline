package org.daisy.pipeline.braille.css.calabash.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.collect.ImmutableList;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.braille.common.saxon.StreamToStreamTransform;
import org.daisy.pipeline.braille.common.TransformationException;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssSplitStep extends DefaultStep {
	
	@Component(
		name = "css:split",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}css-split" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new CssSplitStep(runtime, step);
		}
	}
	
	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;
	
	private static final net.sf.saxon.s9api.QName _SPLIT_BEFORE = new net.sf.saxon.s9api.QName("split-before");
	private static final net.sf.saxon.s9api.QName _SPLIT_AFTER = new net.sf.saxon.s9api.QName("split-after");
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final QName CSS_BOX = new QName(XMLNS_CSS, "box");
	private static final QName _PART = new QName("part");
	
	private CssSplitStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		sourcePipe = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		resultPipe = pipe;
	}
	
	@Override
	public void reset() {
		sourcePipe.resetReader();
		resultPipe.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			XdmNode source = sourcePipe.read();
			Configuration configuration = runtime.getConfiguration().getProcessor().getUnderlyingConfiguration();
			resultPipe.write(
				new CssSplitTransform(configuration,
				                      getSplitPoints(configuration, source,
				                                     getOption(_SPLIT_BEFORE),
				                                     getOption(_SPLIT_AFTER)))
				.transform(source.getUnderlyingNode())); }
		catch (Exception e) {
			logger.error("css:split", e);
			throw new XProcException(step.getNode(), e); }
	}
	
	private static class SplitPoint {
		enum Position { BEFORE, AFTER }
		final List<Integer> path;
		final Position position;
		SplitPoint(List<Integer> path, Position position) {
			this.path = path;
			this.position = position;
		}
	}
	
	private static List<SplitPoint> getSplitPoints(Configuration configuration, XdmNode source,
	                                               RuntimeValue splitBefore, RuntimeValue splitAfter) throws XPathException {
		XPathEvaluator xpathEvaluator = new XPathEvaluator(configuration);
		xpathEvaluator.getStaticContext().setNamespaceResolver(new MatchingNamespaceResolver(splitBefore.getNamespaceBindings()));
		final XPathExpression splitBeforeMatcher = xpathEvaluator.createPattern(splitBefore.getString());
		xpathEvaluator.getStaticContext().setNamespaceResolver(new MatchingNamespaceResolver(splitAfter.getNamespaceBindings()));
		final XPathExpression splitAfterMatcher = xpathEvaluator.createPattern(splitAfter.getString());
		final List<SplitPoint> result = new ArrayList<SplitPoint>();
		new Traversal() {
			List<Integer> currentPath = new ArrayList<Integer>();
			int childCount = 0;
			public void traverse(XdmNode node) {
				if (node.getNodeKind() == XdmNodeKind.DOCUMENT) {
					XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
					while (iter.hasNext()) {
						XdmNode child = (XdmNode)iter.next();
						traverse(child); }}
				else if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
					currentPath.add(++childCount);
					if (matches(splitBeforeMatcher, node))
						if (currentPath.size() > 1)
							result.add(new SplitPoint(ImmutableList.copyOf(currentPath), SplitPoint.Position.BEFORE));
					XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
					childCount = 0;
					while (iter.hasNext()) {
						XdmNode child = (XdmNode)iter.next();
						traverse(child); }
					if (matches(splitAfterMatcher, node))
						if (currentPath.size() > 1)
							result.add(new SplitPoint(ImmutableList.copyOf(currentPath), SplitPoint.Position.AFTER));
					childCount = currentPath.remove(currentPath.size() - 1); }
			}
		}.traverse(source);
		return result;
	}
	
	private static interface Traversal {
		void traverse(XdmNode node);
	}
	
	private static boolean matches(XPathExpression matcher, XdmNode node) {
		try {
			XPathDynamicContext context = matcher.createDynamicContext(node.getUnderlyingNode());
			return matcher.effectiveBooleanValue(context);
		} catch (XPathException e) {
			return false;
		}
	}
	
	private static class CssSplitTransform extends StreamToStreamTransform {
		
		final Iterable<SplitPoint> splitPoints;
		
		CssSplitTransform(Configuration configuration, Iterable<SplitPoint> splitPoints) {
			super(configuration);
			this.splitPoints = splitPoints;
		}
		
		Stack<QName> parents;
		Stack<Map<QName,String>> parentAttrs;
		List<Integer> currentPath;
		Iterator<SplitPoint> splitPointsIterator;
		SplitPoint nextSplitPoint;
		
		protected void _transform(XMLStreamReader reader, BufferedWriter writer) throws TransformationException {
			splitPointsIterator = this.splitPoints.iterator();
			nextSplitPoint = null;
			if (splitPointsIterator.hasNext())
				nextSplitPoint = splitPointsIterator.next();
			parents = new Stack<QName>();
			parentAttrs = new Stack<Map<QName,String>>();
			currentPath = new ArrayList<Integer>();
			int childCount = 0;
			try {
				writer.writeStartElement(new QName("_"));
				while (true)
					try {
						int event = reader.next();
						switch (event) {
						case START_ELEMENT: {
							if (nextSplitPoint == null) {
								writer.copyElement(reader);
								continue; }
							currentPath.add(++childCount);
							if (isSplitPoint(currentPath, nextSplitPoint) && nextSplitPoint.position == SplitPoint.Position.BEFORE)
								split(writer);
							if (containsSplitPoint(currentPath, nextSplitPoint)) {
								writer.copyEvent(event, reader);
								if (CSS_BOX.equals(reader.getName())) {
									for (int i = 0; i < reader.getAttributeCount(); i++) {
										QName name = reader.getAttributeName(i);
										String value = reader.getAttributeValue(i);
										if (_PART.equals(name))
											throw new RuntimeException("input may not have part attributes");
										else
											writer.writeAttribute(name, value); }
									writer.writeAttribute(_PART, "first"); }
								else
									writer.copyAttributes(reader);
								parents.push(reader.getName());
								parentAttrs.push(getAttributeMap(reader));
								childCount = 0; }
							else {
								writer.copyElement(reader);
								if (isSplitPoint(currentPath, nextSplitPoint) && nextSplitPoint.position == SplitPoint.Position.AFTER)
									split(writer);
								childCount = currentPath.remove(currentPath.size() - 1); }
							break; }
						case END_ELEMENT: {
							writer.copyEvent(event, reader);
							parents.pop();
							parentAttrs.pop();
							if (isSplitPoint(currentPath, nextSplitPoint)) {
								if (nextSplitPoint.position != SplitPoint.Position.AFTER)
									throw new RuntimeException("coding error");
								split(writer); }
							childCount = currentPath.remove(currentPath.size() - 1);
							break; }
						default:
							writer.copyEvent(event, reader); }}
					catch (NoSuchElementException e) {
						break; }
				// writer.writeEndElement(); // why does this need to be commented out???
				}
			catch (XMLStreamException e) {
				throw new TransformationException(e); }
		}
		
		void split(Writer writer) throws XMLStreamException {
			nextSplitPoint = splitPointsIterator.hasNext() ? splitPointsIterator.next() : null;
			for (int i = parents.size(); i > 0; i--)
				writer.writeEndElement();
			for (int i = 0; i < parents.size(); i++) {
				writer.writeStartElement(parents.get(i));
				if (CSS_BOX.equals(parents.get(i))) {
					for (Map.Entry<QName,String> attr : parentAttrs.get(i).entrySet()) {
						QName name = attr.getKey();
						String value = attr.getValue();
						if (_PART.equals(name))
							throw new RuntimeException("input may not have part attributes");
						else if (XMLNS_CSS.equals(name.getNamespaceURI())) {
							String localPart = name.getLocalPart();
							if (localPart.equalsIgnoreCase("id") ||
							    localPart.equalsIgnoreCase("string-set") ||
							    localPart.equalsIgnoreCase("string-entry") ||
							    localPart.equalsIgnoreCase("counter-increment") ||
							    localPart.equalsIgnoreCase("counter-set") ||
							    localPart.equalsIgnoreCase("counter-reset") ||
							    localPart.toLowerCase().startsWith("counter-increment-") ||
							    localPart.toLowerCase().startsWith("counter-set-") ||
							    localPart.toLowerCase().startsWith("counter-reset-"));
							else
								writer.writeAttribute(name, value); }
						else
							writer.writeAttribute(name, value); }
					if (containsSplitPoint(currentPath.subList(0, i + 1), nextSplitPoint))
						writer.writeAttribute(_PART, "middle");
					else
						writer.writeAttribute(_PART, "last"); }
				else
					for (Map.Entry<QName,String> attr : parentAttrs.get(i).entrySet())
						writer.writeAttribute(attr.getKey(), attr.getValue()); }
		}
		
		static boolean isSplitPoint(List<Integer> elementPath, SplitPoint splitPoint) {
			if (splitPoint == null)
				return false;
			return elementPath.equals(splitPoint.path);
		}
		
		static boolean containsSplitPoint(List<Integer> elementPath, SplitPoint splitPoint) {
			if (splitPoint == null)
				return false;
			if (elementPath.size() >= splitPoint.path.size())
				return false;
			return splitPoint.path.subList(0, elementPath.size()).equals(elementPath);
		}
		
		static Map<QName,String> getAttributeMap(XMLStreamReader reader) {
			Map<QName,String> map = new HashMap<QName,String>();
			for (int i = 0; i < reader.getAttributeCount(); i++)
				map.put(reader.getAttributeName(i), reader.getAttributeValue(i));
			return map;
		}
	}
	
	// copied from com.xmlcalabash.util.ProcessMatch
	private static class MatchingNamespaceResolver implements NamespaceResolver {
		
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
	
	private static final Logger logger = LoggerFactory.getLogger(CssSplitStep.class);
	
}
