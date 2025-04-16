package org.daisy.pipeline.css.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.URIResolver;

import com.google.common.collect.Iterables;

import cz.vutbr.web.css.CSSProperty.CounterIncrement;
import cz.vutbr.web.css.CSSProperty.CounterReset;
import cz.vutbr.web.css.CSSProperty.CounterSet;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.RuleFactoryImpl;
import cz.vutbr.web.domassign.StyleMap;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.ListStyleType;
import org.daisy.braille.css.RuleCounterStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.pipeline.css.CounterEvaluator;
import org.daisy.pipeline.css.CounterStyle;
import org.daisy.pipeline.css.CssCascader;
import org.daisy.pipeline.css.CssPreProcessor;
import org.daisy.pipeline.css.CssSerializer;
import org.daisy.pipeline.css.JStyleParserCssCascader;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.XsltProcessor;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * For now this cascader handles only 'print' and 'screen' media without support for counters
 * ({@code (counter-support: none)}). It will generate marker contents by <a
 * href="https://www.w3.org/TR/css-lists-3/#content-property">evaluating marker contents</a>, based
 * on {@code ::marker} pseudo-element rules, 'list-style-type', 'counter-reset', 'counter-set' and
 * 'counter-increment' properties and {@code @counter-style} rules, according to <a
 * href="https://www.w3.org/TR/css-lists-3/#content-property">CSS Lists and Counters Module Level
 * 3</a>. The marker contents are inserted as text nodes.
 *
 * For now all other styles are ignored and no attributes are added.
 */
@Component(
	name = "DefaultCssCascader",
	service = { CssCascader.class }
)
public class DefaultCssCascader implements CssCascader {

	public boolean supportsMedium(Medium medium) {
		switch (medium.getType()) {
		case PRINT:
		case SCREEN:
			return "none".equals(medium.getCustomFeatures().get("counter-support"));
		default:
			return false;
		}
	}

	public XMLTransformer newInstance(Medium medium,
	                                  String userStylesheet,
	                                  URIResolver uriResolver,
	                                  CssPreProcessor preProcessor,
	                                  XsltProcessor xsltProcessor,
	                                  QName attributeName,
	                                  boolean multipleAttrs) {
		if (!supportsMedium(medium))
			throw new IllegalArgumentException("medium not supported: " + medium);
		return new Transformer(uriResolver, preProcessor, xsltProcessor, userStylesheet, medium,
		                       attributeName, multipleAttrs);
	}

	// using braille-css because @counter-style is not supported by jStyleParser
	private static final CSSParserFactory parserFactory = new BrailleCSSParserFactory();
	private static final RuleFactory ruleFactory = RuleFactoryImpl.getInstance();
	private static final SupportedBrailleCSS supportedCSS = new SupportedBrailleCSS(false, true); // FIXME: support "list-style" shorthand

	/*
	 * We can make use of JStyleParserCssCascader for the evaluation of marker contents because to
	 * compute the value we only need to know about preceding and ancestor nodes, which is a
	 * condition that is fulfilled because of how traverse() is called.
	 */
	private static class Transformer extends JStyleParserCssCascader {

		private Map<String,CounterStyle> namedCounterStyles = null;
		private CounterEvaluator<Element> evaluator;
		private final QName markerAttributeName;
		private final QName markerContentAttributeName;

		private Transformer(URIResolver resolver, CssPreProcessor preProcessor, XsltProcessor xsltProcessor,
		                    String userStyleSheet, Medium medium, QName attributeName, boolean multipleAttrs) {
			super(resolver, preProcessor, xsltProcessor, userStyleSheet, medium, attributeName,
			      parserFactory, ruleFactory, supportedCSS, supportedCSS);
			if (attributeName == null) {
				markerAttributeName= null;
				markerContentAttributeName = null;
			} else if (multipleAttrs) {
				markerAttributeName= null;
				markerContentAttributeName
					= new QName(attributeName.getNamespaceURI(), "marker-content", attributeName.getPrefix());
			} else {
				markerAttributeName= attributeName;
				markerContentAttributeName = null;
			}
		}

		@Override
		protected void processElement(Element element, StyleMap styleMap, BaseURIAwareXMLStreamWriter writer)
				throws XMLStreamException, IllegalArgumentException {
			if (evaluator == null)
				// styleMap is constant so we can define evaluator based on styleMap argument of first call
				evaluator = new CounterEvaluator<Element>() {
						@Override
						protected Collection<TermPair<String,Integer>> getCounterReset(Element element) {
							NodeData style = styleMap.get(element);
							if (style.getProperty("counter-reset", false) == CounterReset.list_values) {
								List<Term<?>> list = style.getValue(TermList.class, "counter-reset", false);
								if (list != null) {
									for (Term<?> t : list) {
										if (!(t instanceof TermPair))
											throw new IllegalStateException("coding error");
										TermPair p = (TermPair)t;
										if (!(p.getKey() instanceof String))
											throw new IllegalStateException("coding error");
										if (!(p.getValue() instanceof Integer))
											throw new IllegalStateException("coding error");
									}
									return (List<TermPair<String,Integer>>)(List<?>)list;
								}
							}
							return null;
						}
						@Override
						protected Collection<TermPair<String,Integer>> getCounterSet(Element element) {
							NodeData style = styleMap.get(element);
							if (style.getProperty("counter-set", false) == CounterSet.list_values) {
								List<Term<?>> list = style.getValue(TermList.class, "counter-set", false);
								if (list != null) {
									for (Term<?> t : list) {
										if (!(t instanceof TermPair))
											throw new IllegalStateException("coding error");
										TermPair p = (TermPair)t;
										if (!(p.getKey() instanceof String))
											throw new IllegalStateException("coding error");
										if (!(p.getValue() instanceof Integer))
											throw new IllegalStateException("coding error");
									}
									return (List<TermPair<String,Integer>>)(List<?>)list;
								}
							}
							return null;
						}
						@Override
						protected Collection<TermPair<String,Integer>> getCounterIncrement(Element element) {
							NodeData style = styleMap.get(element);
							if (style.getProperty("counter-increment", false) == CounterIncrement.list_values) {
								List<Term<?>> list = style.getValue(TermList.class, "counter-increment", false);
								if (list != null) {
									for (Term<?> t : list) {
										if (!(t instanceof TermPair))
											throw new IllegalStateException("coding error");
										TermPair p = (TermPair)t;
										if (!(p.getKey() instanceof String))
											throw new IllegalStateException("coding error");
										if (!(p.getValue() instanceof Integer))
											throw new IllegalStateException("coding error");
									}
									return (List<TermPair<String,Integer>>)(List<?>)list;
								}
							}
							return null;
						}
						@Override
						protected Display getDisplay(Element element) {
							NodeData style = styleMap.get(element);
							return style.getProperty("display", false);
						}
						@Override
						protected Collection<Term<?>> getMarkerContent(Element element) {
							NodeData style = styleMap.get(element);
							for (PseudoElement pseudo : styleMap.pseudoSet(element)) {
								if ("marker".equals(pseudo.getName())) {
									NodeData pseudoStyle = styleMap.get(element, pseudo);
									if (pseudoStyle.getProperty("content", false) == Content.content_list) {
										List<Term<?>> content = pseudoStyle.getValue(TermList.class, "content", false);
										if (content != null)
											return content;
									}
									return Collections.emptyList();
								}
							}
							return null;
						}
						@Override
						protected CounterStyle getListStyleType(Element element, CounterStyle parentListStyleType) {
							NodeData style = styleMap.get(element);
							ListStyleType type = style.getProperty("list-style-type", false);
							if (type == null)
								return parentListStyleType; // value is inherited from parent
							else {
								switch (type) {
								case INHERIT:
									// should not happen
									return parentListStyleType;
								case INITIAL:
									return CounterStyle.DISC; // see https://www.w3.org/TR/css-lists-3/#propdef-list-style-type
								case counter_style_name:
									return getNamedCounterStyle(style.getValue(TermIdent.class, "list-style-type").getValue());
								case symbols_fn:
									return CounterStyle.fromSymbolsFunction(style.getValue(TermFunction.class, "list-style-type"));
								case braille_string: // note that braille CSS does not support any string, as standard CSS does
									return null;
								case DECIMAL:
									return CounterStyle.DECIMAL;
								case LOWER_ALPHA:
									return CounterStyle.LOWER_ALPHA;
								case LOWER_ROMAN:
									return CounterStyle.LOWER_ROMAN;
								case UPPER_ALPHA:
									return CounterStyle.UPPER_ALPHA;
								case UPPER_ROMAN:
									return CounterStyle.UPPER_ROMAN;
								case NONE:
								default:
									return null;
								}
							}
						}
						@Override
						protected CounterStyle getNamedCounterStyle(String name) {
							if (namedCounterStyles == null)
								namedCounterStyles = CounterStyle.parseCounterStyleRules(
									Iterables.filter(getParsedStyleSheet(), RuleCounterStyle.class));
							return namedCounterStyles.getOrDefault(name, CounterStyle.DECIMAL);
						}
					};
			XMLStreamWriterHelper.writeStartElement(writer, element);
			copyAttributes(element, writer);
			evaluator.startElement(element);
			String marker = evaluator.generateMarkerContents(element);
			if (marker != null) {
				if (markerContentAttributeName != null)
					// if attribute namespace specified, insert as "marker-content" attribute
					XMLStreamWriterHelper.writeAttribute(writer, markerContentAttributeName, marker);
				else if (markerAttributeName != null)
					// if single attribute specified, insert as ::marker {} pseudo-element rule
					XMLStreamWriterHelper.writeAttribute(
						writer, markerAttributeName,
						String.format("&::marker { content: '%s' }", marker.replaceAll("'", "\\\\'")));
				else
					// if no attribute specified, insert as text
					writer.writeCharacters(marker);
			}
			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
				traverse(child, styleMap, writer);
			evaluator.endElement();
			writer.writeEndElement();
		}

		protected Map<QName,String> serializeStyle(NodeData mainStyle, Map<PseudoElement,NodeData> pseudoStyles, Element context) {
			throw new UnsupportedOperationException(); // not needed
		}

		protected String serializeValue(Term<?> value) {
			return CssSerializer.toString(value);
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(DefaultCssCascader.class);

}
