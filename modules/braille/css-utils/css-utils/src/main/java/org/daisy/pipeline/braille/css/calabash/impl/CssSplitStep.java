package org.daisy.pipeline.braille.css.calabash.impl;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.XPathException;

import org.daisy.common.calabash.XMLCalabashHelper;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.NodeToXMLStreamTransformer;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.getAttributes;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttributes;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeElement;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeEvent;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.xproc.calabash.XProcStepProvider;

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
			XMLCalabashHelper.transform(
				new CssSplitTransformer(getOption(_SPLIT_BEFORE), getOption(_SPLIT_AFTER), runtime),
				sourcePipe,
				resultPipe,
				runtime); }
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
		
		final XPathExpression splitBeforeMatcher = splitBefore.getString().isEmpty()
			? null
			: SaxonHelper.compileExpression(splitBefore.getString(), splitBefore.getNamespaceBindings(), configuration);
		final XPathExpression splitAfterMatcher = splitAfter.getString().isEmpty()
			? null
			: SaxonHelper.compileExpression(splitAfter.getString(), splitAfter.getNamespaceBindings(), configuration);
		final List<SplitPoint> result = new ArrayList<SplitPoint>();
		new Consumer<XdmNode>() {
			List<Integer> currentPath = new ArrayList<Integer>();
			int childCount = 0;
			public void accept(XdmNode node) {
				if (node.getNodeKind() == XdmNodeKind.DOCUMENT) {
					XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
					while (iter.hasNext()) {
						XdmNode child = (XdmNode)iter.next();
						accept(child); }}
				else if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
					currentPath.add(++childCount);
					if (splitBeforeMatcher != null)
						if (SaxonHelper.evaluateBoolean(splitBeforeMatcher, node))
							if (currentPath.size() > 1)
								result.add(new SplitPoint(ImmutableList.copyOf(currentPath), SplitPoint.Position.BEFORE));
					XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
					childCount = 0;
					while (iter.hasNext()) {
						XdmNode child = (XdmNode)iter.next();
						accept(child); }
					if (splitAfterMatcher != null)
						if (SaxonHelper.evaluateBoolean(splitAfterMatcher, node))
							if (currentPath.size() > 1)
								result.add(new SplitPoint(ImmutableList.copyOf(currentPath), SplitPoint.Position.AFTER));
					childCount = currentPath.remove(currentPath.size() - 1); }
			}
		}.accept(source);
		return result;
	}
	
	private static class CssSplitTransformer implements NodeToXMLStreamTransformer {
		
		final Configuration config;
		final RuntimeValue splitBefore;
		final RuntimeValue splitAfter;
		
		CssSplitTransformer(RuntimeValue splitBefore, RuntimeValue splitAfter, XProcRuntime runtime) {
			this.config = runtime.getProcessor().getUnderlyingConfiguration();
			this.splitBefore = splitBefore;
			this.splitAfter = splitAfter;
		}
		
		Stack<QName> parents;
		Stack<Map<QName,String>> parentAttrs;
		List<Integer> currentPath;
		Iterator<SplitPoint> splitPoints;
		SplitPoint nextSplitPoint;
		
		@Override
		public void transform(Iterator<XdmNode> input, Supplier<BaseURIAwareXMLStreamWriter> output) throws TransformerException {
			XdmNode doc = Iterators.getOnlyElement(input);
			XMLStreamReader reader;
			try {
				reader = SaxonHelper.nodeReader(doc, config);
				splitPoints = getSplitPoints(config, doc, splitBefore, splitAfter).iterator();
			} catch (XPathException e) {
				throw new TransformerException(e);
			}
			transform(reader, output);
		}
		
		void transform(XMLStreamReader reader, Supplier<BaseURIAwareXMLStreamWriter> output) throws TransformerException {
			XMLStreamWriter writer = output.get();
			nextSplitPoint = null;
			if (splitPoints.hasNext())
				nextSplitPoint = splitPoints.next();
			parents = new Stack<QName>();
			parentAttrs = new Stack<Map<QName,String>>();
			currentPath = new ArrayList<Integer>();
			int childCount = 0;
			try {
				writer.writeStartDocument();
				writeStartElement(writer, new QName("_"));
			  loop: while (true)
					try {
						int event = reader.next();
						switch (event) {
						case START_ELEMENT: {
							if (nextSplitPoint == null) {
								writeElement(writer, reader);
								continue; }
							currentPath.add(++childCount);
							if (isSplitPoint(currentPath, nextSplitPoint) && nextSplitPoint.position == SplitPoint.Position.BEFORE)
								split(writer);
							if (containsSplitPoint(currentPath, nextSplitPoint)) {
								writeEvent(writer, event, reader);
								if (CSS_BOX.equals(reader.getName())) {
									for (int i = 0; i < reader.getAttributeCount(); i++) {
										QName name = reader.getAttributeName(i);
										String value = reader.getAttributeValue(i);
										if (_PART.equals(name))
											throw new RuntimeException("input may not have part attributes");
										else
											writeAttribute(writer, name, value); }
									writeAttribute(writer, _PART, "first"); }
								else
									writeAttributes(writer, reader);
								parents.push(reader.getName());
								parentAttrs.push(getAttributes(reader));
								childCount = 0; }
							else {
								writeElement(writer, reader);
								if (isSplitPoint(currentPath, nextSplitPoint) && nextSplitPoint.position == SplitPoint.Position.AFTER)
									split(writer);
								childCount = currentPath.remove(currentPath.size() - 1); }
							break; }
						case END_ELEMENT: {
							writeEvent(writer, event, reader);
							parents.pop();
							parentAttrs.pop();
							if (isSplitPoint(currentPath, nextSplitPoint)) {
								if (nextSplitPoint.position != SplitPoint.Position.AFTER)
									throw new RuntimeException("coding error");
								split(writer); }
							childCount = currentPath.remove(currentPath.size() - 1);
							break; }
						case START_DOCUMENT:
							break;
						case END_DOCUMENT:
							break loop;
						default:
							writeEvent(writer, event, reader); }}
					catch (NoSuchElementException e) {
						break; }
				writer.writeEndElement();
				writer.writeEndDocument();
			} catch (XMLStreamException e) {
				throw new TransformerException(e); }
		}
		
		void split(XMLStreamWriter writer) throws XMLStreamException {
			nextSplitPoint = splitPoints.hasNext() ? splitPoints.next() : null;
			for (int i = parents.size(); i > 0; i--)
				writer.writeEndElement();
			for (int i = 0; i < parents.size(); i++) {
				writeStartElement(writer, parents.get(i));
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
								writeAttribute(writer, name, value); }
						else
							writeAttribute(writer, name, value); }
					if (containsSplitPoint(currentPath.subList(0, i + 1), nextSplitPoint))
						writeAttribute(writer, _PART, "middle");
					else
						writeAttribute(writer, _PART, "last"); }
				else
					for (Map.Entry<QName,String> attr : parentAttrs.get(i).entrySet())
						writeAttribute(writer, attr.getKey(), attr.getValue()); }
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
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CssSplitStep.class);
	
}
