package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.XMLConstants;

import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import cz.vutbr.web.css.Term;

import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.PropertyValue;
import org.daisy.common.saxon.SaxonBuffer;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper;
import static org.daisy.common.stax.XMLStreamWriterHelper.skipElement;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttributes;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeEvent;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.braille.css.CounterFunction;
import org.daisy.pipeline.braille.css.CounterSet;
import org.daisy.pipeline.braille.css.TextStyleParser;
import org.daisy.pipeline.braille.css.xpath.Style;
import org.daisy.pipeline.css.CounterEvaluator;
import org.daisy.pipeline.css.CounterStyle;
import org.daisy.pipeline.css.CssSerializer;

import org.osgi.service.component.annotations.Component;

public class EvalCounterStep extends DefaultStep implements XProcStep {

	@Component(
		name = "pxi:eval-counter-internal",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}eval-counter-internal" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new EvalCounterStep(runtime, step);
		}
	}

	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;

	private EvalCounterStep(XProcRuntime runtime, XAtomicStep step) {
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

	private final static net.sf.saxon.s9api.QName _COUNTER_NAMES = new net.sf.saxon.s9api.QName(new QName("counter-names"));
	private final static net.sf.saxon.s9api.QName _EXCLUDE_COUNTER_NAMES = new net.sf.saxon.s9api.QName(new QName("exclude-counter-names"));
	private final static net.sf.saxon.s9api.QName _COUNTER_STYLES = new net.sf.saxon.s9api.QName(new QName("counter-styles"));

	private final static TextStyleParser cssParser = TextStyleParser.getInstance();

	@Override
	public void run() throws SaxonApiException {
		try {
			Set<String> counterNames
				= Sets.newHashSet(
					SaxonHelper.iterableFromSequence(getOption(_COUNTER_NAMES).getValue().getUnderlyingValue(), String.class));
			Set<String> excludeCounterNames
				= Sets.newHashSet(
					SaxonHelper.iterableFromSequence(getOption(_EXCLUDE_COUNTER_NAMES).getValue().getUnderlyingValue(), String.class));
			Item map = SaxonHelper.getSingleItem(getOption(_COUNTER_STYLES).getValue().getUnderlyingValue());
			if (!(map instanceof MapItem))
				throw new IllegalArgumentException();
			Map<String,CounterStyle> namedCounterStyles = Maps.transformValues(
				SaxonHelper.mapFromMapItem((MapItem)map, Style.class),
				s -> {
					if (!(s instanceof CounterStyle))
						throw new IllegalArgumentException();
					return (CounterStyle)s; });
			Predicate<String> counterFilter = counterNames.contains("#all")
				? ((Predicate<String>)excludeCounterNames::contains).negate()
				: counterNames::contains;
			// first resolve counter() functions and compute counter values at all targets of target-counter() functions
			PersistentCounterEvaluator normalFlowCounterEvaluator = new PersistentCounterEvaluator();
			new EvalCounter(namedCounterStyles,
			                counterFilter,
			                normalFlowCounterEvaluator,
			                () -> new CounterEvaluatorImpl())
				// then resolve target-counter() functions
				.andThen(new EvalTargetCounter(namedCounterStyles,
				                               counterFilter,
				                               normalFlowCounterEvaluator),
				         new SaxonBuffer(runtime.getProcessor().getUnderlyingConfiguration()),
				         false)
				.transform(
					XMLCalabashInputValue.of(sourcePipe),
					XMLCalabashOutputValue.of(resultPipe, runtime))
				.run();
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final QName CSS_COUNTER_RESET = new QName(XMLNS_CSS, "counter-reset", "css");
	private static final QName CSS_COUNTER_SET = new QName(XMLNS_CSS, "counter-set", "css");
	private static final QName CSS_COUNTER_INCREMENT = new QName(XMLNS_CSS, "counter-increment", "css");
	private static final QName CSS_COUNTER = new QName(XMLNS_CSS, "counter", "css");
	private static final QName _TARGET = new QName("target");
	private static final QName _NAME = new QName("name");
	private static final QName _STYLE = new QName("style");
	private static final QName CSS_BOX = new QName(XMLNS_CSS, "box", "css");
	private static final QName _TYPE = new QName("type");
	private static final QName XML_LANG = new QName(XMLConstants.XML_NS_URI, "lang", XMLConstants.XML_NS_PREFIX);
	private static final QName CSS_MARKER = new QName(XMLNS_CSS, "marker", "css");
	private static final QName CSS_ID = new QName(XMLNS_CSS, "id", "css");
	private static final QName CSS_ANCHOR = new QName(XMLNS_CSS, "anchor", "css");
	private static final QName CSS_FLOW = new QName(XMLNS_CSS, "flow", "css");

	private static class EvalCounter extends SingleInSingleOutXMLTransformer {

		private final Map<String,CounterStyle> namedCounterStyles;
		private final Predicate<String> counterFilter;
		private final PersistentCounterEvaluator normalFlowCounterEvaluator;
		private final Supplier<CounterEvaluator<ElementStyle>> counterEvaluatorFactory;

		public EvalCounter(Map<String,CounterStyle> namedCounterStyles,
		                   Predicate<String> counterFilter,
		                   PersistentCounterEvaluator normalFlowCounterEvaluator,
		                   Supplier<CounterEvaluator<ElementStyle>> counterEvaluatorFactory) {
			this.namedCounterStyles = namedCounterStyles;
			this.counterFilter = counterFilter;
			this.normalFlowCounterEvaluator = normalFlowCounterEvaluator;
			this.counterEvaluatorFactory = counterEvaluatorFactory;
		}

		public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
			if (source == null || result == null)
				throw new IllegalArgumentException();
			return () -> transform(source.asXMLStreamReader(), result.asXMLStreamWriter());
		}

		public void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter writer) throws TransformerException {
			CounterEvaluator<ElementStyle> evaluator = normalFlowCounterEvaluator;
			Predicate<String> counterFilterNegated = counterFilter.negate();
			boolean nextElementIsRoot = false;
			boolean inNormalFlow = false;
			int depth = 0;
			int insideMarker = -1;
			int insideElementWithAnchor = -1;
			String anchor = null;
			try {
				int event = reader.getEventType();
				while (true)
					try {
						switch (event) {
						case START_DOCUMENT: {
							writer.setBaseURI(reader.getBaseURI());
							nextElementIsRoot = true;
							writeEvent(writer, reader);
							break; }
						case START_ELEMENT: {
							QName elem = reader.getName();
							if (nextElementIsRoot) {
								inNormalFlow = isPartOfNormalFlow(reader);
								if (inNormalFlow)
									evaluator = normalFlowCounterEvaluator;
								else
									evaluator = counterEvaluatorFactory.get();
								nextElementIsRoot = false; }
							ElementStyle elemStyle = null;
							if (CSS_COUNTER.equals(elem)) {
								String name = null;
								String style = null;
								boolean hasTarget = false; {
									hasTarget = anchor != null;
									for (int i = 0; i < reader.getAttributeCount(); i++) {
										QName n = reader.getAttributeName(i);
										if (!hasTarget && _TARGET.equals(n)) {
											hasTarget = true;
											break; }
										else if (name == null && _NAME.equals(n))
											name = reader.getAttributeValue(i);
										else if (style == null && _STYLE.equals(n))
											style = reader.getAttributeValue(i); }}
								if (!hasTarget && name != null && counterFilter.test(name)) {
									CounterStyle counterStyle = parseCounterStyle(style, namedCounterStyles);
									if (counterStyle != null) {
										int value = evaluator.evaluateCounter(name);
										writeStartElement(writer, CSS_BOX);
										writeAttribute(writer, _TYPE, "inline");
										writeAttribute(writer, XML_LANG, "");
										writeAttribute(writer, _STYLE, String.format("text-transform: %s", counterStyle.getTextTransform(value)));
										writer.writeCharacters(counterStyle.format(value, insideMarker >= 0));
										writer.writeEndElement();
									}
									skipElement(reader);
									break;
								}
							} else {
								elemStyle = new ElementStyle(reader);
								evaluator.startElement(elemStyle.filter(counterFilter));
								if (evaluator == normalFlowCounterEvaluator) {
									String id = null; {
										for (int i = 0; i < reader.getAttributeCount(); i++)
											if (CSS_ID.equals(reader.getAttributeName(i))) {
												id = reader.getAttributeValue(i);
												break; }}
									if (id != null)
										normalFlowCounterEvaluator.saveState(id); }
								if (insideMarker < 0)
									if (CSS_MARKER.equals(elem))
										insideMarker = depth;
									else if (CSS_BOX.equals(elem))
										for (int i = 0; i < reader.getAttributeCount(); i++)
											if (_NAME.equals(reader.getAttributeName(i))) {
												if ("css:marker".equals(reader.getAttributeValue(i)))
													insideMarker = depth;
												break; }
								if (!inNormalFlow && insideElementWithAnchor < 0)
									for (int i = 0; i < reader.getAttributeCount(); i++)
										if (CSS_ANCHOR.equals(reader.getAttributeName(i))) {
											insideElementWithAnchor = depth;
											anchor = reader.getAttributeValue(i);
											break; }
							}
							writeEvent(writer, reader);
							// clean up attributes
							if (CSS_COUNTER.equals(elem))
								writeAttributes(writer, reader);
							else {
								elemStyle = elemStyle.filter(counterFilterNegated);
								for (int i = 0; i < reader.getAttributeCount(); i++) {
									QName n = reader.getAttributeName(i);
									if (CSS_COUNTER_RESET.equals(n))
										writeAttribute(writer, n, elemStyle.counterReset);
									else if (CSS_COUNTER_SET.equals(n))
										writeAttribute(writer, n, elemStyle.counterSet);
									else if (CSS_COUNTER_INCREMENT.equals(n))
										writeAttribute(writer, n, elemStyle.counterIncrement);
									else
										writeAttribute(writer, n, reader.getAttributeValue(i)); }}
							depth++;
							break; }
						case END_ELEMENT: {
							if (!CSS_COUNTER.equals(reader.getName()))
								evaluator.endElement();
							writeEvent(writer, reader);
							depth--;
							if (insideMarker == depth)
								insideMarker = -1;
							if (!inNormalFlow && insideElementWithAnchor == depth) {
								insideElementWithAnchor = -1;
								anchor = null; }
							break; }
						default:
							writeEvent(writer, reader);
						}
						event = reader.next();
					} catch (NoSuchElementException e) {
						break;
					}
				writer.flush();
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
		}

		private static void writeAttribute(XMLStreamWriter writer, QName name, String value) throws XMLStreamException {
			XMLStreamWriterHelper.writeAttribute(writer, name, value);
		}

		private static void writeAttribute(XMLStreamWriter writer, QName name, Collection<CounterSet> value) throws XMLStreamException {
			if (value != null && !value.isEmpty()) {
				String v = CssSerializer.getInstance().serializeTermList(value);
				if (!v.isEmpty())
					writeAttribute(writer, name, v);
			}
		}
	}

	private static class EvalTargetCounter extends SingleInSingleOutXMLTransformer {

		private final Map<String,CounterStyle> namedCounterStyles;
		private final Predicate<String> counterFilter;
		private final PersistentCounterEvaluator normalFlowCounterEvaluator;

		public EvalTargetCounter(Map<String,CounterStyle> namedCounterStyles,
		                         Predicate<String> counterFilter,
		                         PersistentCounterEvaluator normalFlowCounterEvaluator) {
			this.namedCounterStyles = namedCounterStyles;
			this.counterFilter = counterFilter;
			this.normalFlowCounterEvaluator = normalFlowCounterEvaluator;
		}

		public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
			if (source == null || result == null)
				throw new IllegalArgumentException();
			return () -> transform(source.asXMLStreamReader(), result.asXMLStreamWriter());
		}

		public void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter writer) throws TransformerException {
			boolean nextElementIsRoot = false;
			boolean inNormalFlow = false;
			int depth = 0;
			int insideMarker = -1;
			int insideElementWithAnchor = -1;
			String anchor = null;
			try {
				int event = reader.getEventType();
				while (true)
					try {
						switch (event) {
						case START_DOCUMENT: {
							writer.setBaseURI(reader.getBaseURI());
							nextElementIsRoot = true;
							writeEvent(writer, reader);
							break; }
						case START_ELEMENT: {
							if (nextElementIsRoot) {
								inNormalFlow = isPartOfNormalFlow(reader);
								nextElementIsRoot = false; }
							QName elem = reader.getName();
							if (CSS_COUNTER.equals(elem)) {
								String name = null;
								String style = null;
								String target = null; {
									for (int i = 0; i < reader.getAttributeCount(); i++) {
										QName n = reader.getAttributeName(i);
										if (target == null && _TARGET.equals(n))
											target = reader.getAttributeValue(i);
										else if (name == null && _NAME.equals(n))
											name = reader.getAttributeValue(i);
										else if (style == null && _STYLE.equals(n))
											style = reader.getAttributeValue(i); }}
								if (target == null)
									target = anchor;
								if (target != null && name != null && counterFilter.test(name)) {
									Integer value = null; {
										try {
											value = normalFlowCounterEvaluator.evaluateCounter(name, target); }
										catch (IllegalStateException e) {
											/* target does not exist */ }}
									if (value != null) {
										// target exists and counter exists
										CounterStyle counterStyle = parseCounterStyle(style, namedCounterStyles);
										if (counterStyle != null) {
											writeStartElement(writer, CSS_BOX);
											writeAttribute(writer, _TYPE, "inline");
											writeAttribute(writer, XML_LANG, "");
											writeAttribute(writer, _STYLE, String.format("text-transform: %s", counterStyle.getTextTransform(value)));
											writer.writeCharacters(counterStyle.format(value, insideMarker >= 0));
											writer.writeEndElement();
										}
									}
									skipElement(reader);
									break;
								}
							} else {
								if (insideMarker < 0)
									if (CSS_MARKER.equals(elem))
										insideMarker = depth;
									else if (CSS_BOX.equals(elem))
										for (int i = 0; i < reader.getAttributeCount(); i++)
											if (_NAME.equals(reader.getAttributeName(i))) {
												if ("css:marker".equals(reader.getAttributeValue(i)))
													insideMarker = depth;
												break; }
								if (!inNormalFlow && insideElementWithAnchor < 0)
									for (int i = 0; i < reader.getAttributeCount(); i++)
										if (CSS_ANCHOR.equals(reader.getAttributeName(i))) {
											insideElementWithAnchor = depth;
											anchor = reader.getAttributeValue(i);
											break; }
							}
							// make sure css:* elements preserve their prefix
							if (XMLNS_CSS.equals(elem.getNamespaceURI()))
								writeStartElement(writer, new QName(elem.getNamespaceURI(), elem.getLocalPart(), "css"));
							else
								writeEvent(writer, reader);
							writeAttributes(writer, reader);
							depth++;
							break; }
						case END_ELEMENT: {
							writeEvent(writer, reader);
							depth--;
							if (insideMarker == depth)
								insideMarker = -1;
							if (!inNormalFlow && insideElementWithAnchor == depth) {
								insideElementWithAnchor = -1;
								anchor = null; }
							break; }
						default:
							writeEvent(writer, reader);
						}
						event = reader.next();
					} catch (NoSuchElementException e) {
						break;
					}
				writer.flush();
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
		}
	}

	// /*[not(@css:flow[not(.='normal')])]
	private static boolean isPartOfNormalFlow(XMLStreamReader rootElement) {
		for (int i = 0; i < rootElement.getAttributeCount(); i++)
			if (CSS_FLOW.equals(rootElement.getAttributeName(i)))
				return "normal".equals(rootElement.getAttributeValue(i));
		return true;
	}

	/**
	 * Parse {@code style} attribute of {@code css:counter} element. Return {@code null} if {@code
	 * none} was specified.
	 */
	private static CounterStyle parseCounterStyle(String attr, Map<String,CounterStyle> namedCounterStyles) {
		// style attribute contains counter style name or symbols() function or string
		if ("none".equals(attr))
			return null;
		CounterStyle style = namedCounterStyles.get(attr);
		if (style == null)
			style = CounterStyle.predefined(attr);
		if (style == null) {
			PropertyValue d = cssParser.parse(
				String.format("content: counter(x, %s)", attr)).get("content");
			if (d.getValue() instanceof List) {
				List<?> list = (List<?>)d.getValue();
				if (list.size() == 1 && list.get(0) instanceof CounterFunction) {
					CounterFunction f = (CounterFunction)list.get(0);
					style = f.getStyle().orElse(null); }}}
		if (style == null)
			style = CounterStyle.DECIMAL;
		return style;
	}

	/**
	 * {@code CounterEvaluator} that remembers the counter values at certain positions.
	 */
	private static class PersistentCounterEvaluator extends CounterEvaluatorImpl {

		// Currently not using a persistent data structure. Doing that could improve the efficiency.
		private final Map<String,Map<String,Integer>> saved = new HashMap<>();

		/**
		 * Save the counter values at the current location and associate it with the given
		 * identifier.
		 */
		public void saveState(String id) {
			Map<String,Integer> state = new HashMap<>();
			if (saved.put(id, state) != null)
				; // throw new IllegalStateException("already saved: " + id);
			// flatten map
			for (Map<String,Integer> c : counterValues)
				if (c != null)
					for (String n : c.keySet())
						if (!state.containsKey(n))
							state.put(n, c.get(n));
		}

		/**
		 * Get the value of the innermost counter in the counter set with the given name and at the
		 * given position in the document, or {@code null} if there is no counter with that name at
		 * the given position.
		 *
		 * @throws IllegalStateException if {@link #saveState} has not been previously called with
		 *                               the given {@code id}.
		 */
		public Integer evaluateCounter(String name, String id) throws IllegalStateException {
			Map<String,Integer> state = saved.get(id);
			if (state == null)
				throw new IllegalStateException("not saved: " + id);
			return state.get(name);
		}
	}

	public static class ElementStyle {

		public final Collection<CounterSet> counterReset;
		public final Collection<CounterSet> counterSet;
		public final Collection<CounterSet> counterIncrement;

		private ElementStyle(XMLStreamReader element) {
			counterReset = getPairList(element, CSS_COUNTER_RESET);
			counterSet = getPairList(element, CSS_COUNTER_SET);
			counterIncrement = getPairList(element, CSS_COUNTER_INCREMENT);
		}

		private ElementStyle(Collection<CounterSet> counterReset,
		                     Collection<CounterSet> counterSet,
		                     Collection<CounterSet> counterIncrement) {
			this.counterReset = counterReset;
			this.counterSet = counterSet;
			this.counterIncrement = counterIncrement;
		}

		private List<CounterSet> getPairList(XMLStreamReader element, QName attributeName) {
			for (int i = 0; i < element.getAttributeCount(); i++)
				if (attributeName.equals(element.getAttributeName(i))) {
					String p = attributeName.getLocalPart();
					PropertyValue d = cssParser.parse(
						String.format("%s: %s", p, element.getAttributeValue(i))).get(p);
					if (d.getValue() instanceof List) {
						List<?> list = (List<?>)d.getValue();
						// assume that it is a List of CounterSet
						return (List<CounterSet>)list;
					}
					break;
				}
			return null;
		}

		public ElementStyle filter(Predicate<String> counterFilter) {
			return new ElementStyle(filter(counterReset, counterFilter),
			                        filter(counterSet, counterFilter),
			                        filter(counterIncrement, counterFilter));
		}

		private static Collection<CounterSet> filter(Collection<CounterSet> list, Predicate<String> filter) {
			return list != null
				? Collections2.filter(list, x -> filter.test(x.getKey()))
				: null;
		}
	}

	private static class CounterEvaluatorImpl extends CounterEvaluator<ElementStyle> {

		@Override
		protected Collection<CounterSet> getCounterReset(ElementStyle element) {
			return element.counterReset;
		}

		@Override
		protected Collection<CounterSet> getCounterSet(ElementStyle element) {
			return element.counterSet;
		}

		@Override
		protected Collection<CounterSet> getCounterIncrement(ElementStyle element) {
			return element.counterIncrement;
		}

		@Override
		protected Display getDisplay(ElementStyle element) {
			// incrementing 'list-item' counter is done in make-boxes.xsl
			return Display.INLINE;
		}

		@Override
		protected List<Term<?>> getMarkerContent(ElementStyle element) {
			// does not matter because we're not calling generateMarkerContents()
			return null;
		}

		@Override
		protected CounterStyle getListStyleType(ElementStyle element, CounterStyle parentListStyleType) {
			// does not matter because we're not calling generateMarkerContents()
			return null;
		}

		@Override
		protected CounterStyle getNamedCounterStyle(String name) {
			// does not matter because we're not calling generateMarkerContents()
			return null;
		}
	}
}
