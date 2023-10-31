package org.daisy.pipeline.css.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.RuleFactoryImpl;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.StyleMap;

import org.daisy.braille.css.BrailleCSSDeclarationTransformer;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.ListStyleType;
import org.daisy.braille.css.RuleCounterStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper;
import org.daisy.common.transform.XMLTransformer;
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
 * ({@code (-counter-support: none)}). It will generate marker contents by <a
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
	private static final SupportedCSS supportedCSS = new SupportedBrailleCSS(false, true); // FIXME: support "list-style" shorthand
	private static final DeclarationTransformer declarationTransformer
		= new BrailleCSSDeclarationTransformer(supportedCSS);

	/*
	 * We can make use of JStyleParserCssCascader for the evaluation of marker contents because to
	 * compute the value we only need to know about preceding and ancestor nodes, which is a
	 * condition that is fulfilled because of how traverse() is called.
	 */
	private static class Transformer extends JStyleParserCssCascader {

		private Map<String,CounterStyle> namedCounterStyles = null;
		private LinkedList<CounterStyle> listCounterStyle = new LinkedList<>();
		private LinkedList<Map<String,Integer>> counterValues = new LinkedList<>();
		private final QName markerAttributeName;
		private final QName markerContentAttributeName;

		private Transformer(URIResolver resolver, CssPreProcessor preProcessor, XsltProcessor xsltProcessor,
		                    String userStyleSheet, Medium medium, QName attributeName, boolean multipleAttrs) {
			super(resolver, preProcessor, xsltProcessor, userStyleSheet, medium, attributeName,
			      parserFactory, ruleFactory, supportedCSS, declarationTransformer);
			listCounterStyle.push(CounterStyle.DISC);
			counterValues.push(null);
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
			XMLStreamWriterHelper.writeStartElement(writer, element);
			copyAttributes(element, writer);
			Map<PseudoElement,NodeData> pseudoStyles = new HashMap<>(); {
				for (PseudoElement pseudo : styleMap.pseudoSet(element))
					pseudoStyles.put(pseudo, styleMap.get(element, pseudo)); }
			NodeData mainStyle = styleMap.get(element);
			updateCounterValues(mainStyle);
			CounterStyle listCounterStyle = getCurrentListCounterStyle(mainStyle);
			String marker = generateMarkerContents(mainStyle, pseudoStyles, element, listCounterStyle);
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
			this.listCounterStyle.push(listCounterStyle);
			this.counterValues.push(null);
			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
				traverse(child, styleMap, writer);
			this.listCounterStyle.pop();
			this.counterValues.pop();
			writer.writeEndElement();
		}

		/**
		 * Determine counter values (see https://www.w3.org/TR/css-lists-3/#creating-counters)
		 */
		private void updateCounterValues(NodeData style) {
			if (style.getProperty("counter-reset", false) == CounterReset.list_values) {
				List<Term<?>> list = style.getValue(TermList.class, "counter-reset", false);
				if (list != null) {
					for (Term<?> t : list) {
						if (!(t instanceof TermPair))
							throw new IllegalStateException("coding error");
						TermPair<String,Integer> p = (TermPair<String,Integer>)t;
						String name = p.getKey();
						Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
						if (createdBySiblingOrSelf == null) {
							createdBySiblingOrSelf = new HashMap<>();
							counterValues.set(0, createdBySiblingOrSelf);
						}
						createdBySiblingOrSelf.put(name, p.getValue());
					}
				}
			}
			// counter-set or counter-increment work in addition to counter-reset
			Set<String> done = null;
			if (style.getProperty("counter-set", false) == CounterSet.list_values) {
				List<Term<?>> list = style.getValue(TermList.class, "counter-set", false);
				if (list != null) {
					for (Term<?> t : list) {
						if (!(t instanceof TermPair))
							throw new IllegalStateException("coding error");
						TermPair<String,Integer> p = (TermPair<String,Integer>)t;
						String name = p.getKey();
						boolean exists = false;
						for (Map<String,Integer> c : counterValues) {
							if (c != null) {
								c.put(name, p.getValue());
								exists = true;
								break;
							}
						}
						if (!exists) {
							Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
							if (createdBySiblingOrSelf == null) {
								createdBySiblingOrSelf = new HashMap<>();
								counterValues.set(0, createdBySiblingOrSelf);
							}
							createdBySiblingOrSelf.put(name, p.getValue());
						}
						if (done == null)
							done = new HashSet<>();
						done.add(name);
					}
				}
			}
			if (style.getProperty("counter-increment", false) == CounterIncrement.list_values) {
				List<Term<?>> list = style.getValue(TermList.class, "counter-increment", false);
				if (list != null) {
					for (Term<?> t : list) {
						if (!(t instanceof TermPair))
							throw new IllegalStateException("coding error");
						TermPair<String,Integer> p = (TermPair<String,Integer>)t;
						String name = p.getKey();
						if (done == null || !done.contains(name)) {
							boolean exists = false;
							for (Map<String,Integer> c : counterValues) {
								if (c != null) {
									c.put(name, c.getOrDefault(name, 0) + p.getValue());
									exists = true;
									break;
								}
							}
							if (!exists) {
								Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
								if (createdBySiblingOrSelf == null) {
									createdBySiblingOrSelf = new HashMap<>();
									counterValues.set(0, createdBySiblingOrSelf);
								}
								createdBySiblingOrSelf.put(name, p.getValue());
							}
							if (done == null)
								done = new HashSet<>();
							done.add(name);
						}
					}
				}
			}
			// by default list items increment the 'list-item' counter
			if ((done == null || !done.contains("list-item")) && style.getProperty("display", false) == Display.LIST_ITEM) {
				boolean exists = false;
				for (Map<String,Integer> c : counterValues) {
					if (c != null) {
						c.put("list-item", c.getOrDefault("list-item", 0) + 1);
						exists = true;
						break;
					}
				}
				if (!exists) {
					Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
					if (createdBySiblingOrSelf == null) {
						createdBySiblingOrSelf = new HashMap<>();
						counterValues.set(0, createdBySiblingOrSelf);
					}
					createdBySiblingOrSelf.put("list-item", 1);
				}
			}
		}

		private CounterStyle getCurrentListCounterStyle(NodeData style) {
			ListStyleType type = style.getProperty("list-style-type", false);
			if (type == null)
				return this.listCounterStyle.peek(); // value is inherited from parent
			else {
				switch (type) {
				case INHERIT:
					// should not happen
					return this.listCounterStyle.peek();
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

		private CounterStyle getNamedCounterStyle(String name) {
			if (namedCounterStyles == null)
				namedCounterStyles = CounterStyle.parseCounterStyleRules(
					Iterables.filter(getParsedStyleSheet(), RuleCounterStyle.class));
			return namedCounterStyles.getOrDefault(name, CounterStyle.DECIMAL);
		}

		private String evaluateCounter(String name, CounterStyle style, boolean withPrefixAndSuffix) {
			Integer value = null;
			boolean exists = false;
			for (Map<String,Integer> c : counterValues)
				if (c != null && c.containsKey(name)) {
					value = c.get(name);
					break;
				}
			if (value == null) {
				value = 0;
				Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
				if (createdBySiblingOrSelf == null) {
					createdBySiblingOrSelf = new HashMap<>();
					counterValues.set(0, createdBySiblingOrSelf);
				}
				createdBySiblingOrSelf.put(name, value);
			}
			return style.format(value, withPrefixAndSuffix);
		}

		private String evaluateCounters(String name, CounterStyle style, String separator) {
			StringBuilder s = new StringBuilder();
			for (Map<String,Integer> c : counterValues)
				if (c != null && c.containsKey(name)) {
					if (s.length() > 0)
						s.insert(0, separator);
					s.insert(0, style.format(c.get(name)));
				}
			if (s.length() == 0) {
				Map<String,Integer> createdBySiblingOrSelf = counterValues.peek();
				if (createdBySiblingOrSelf == null) {
					createdBySiblingOrSelf = new HashMap<>();
					counterValues.set(0, createdBySiblingOrSelf);
				}
				createdBySiblingOrSelf.put(name, 0);
				s.append(style.format(0));
			}
			return s.toString();
		}

		private String evaluateContent(List<Term<?>> content, Element element) throws IllegalArgumentException {
			StringBuilder s = new StringBuilder();
			for (Term<?> t : content) {
				if (t instanceof TermString) {
					s.append(((TermString)t).getValue());
				} else if (t instanceof TermFunction) {
					TermFunction f = (TermFunction)t;
					if ("counter".equalsIgnoreCase(f.getFunctionName())) {
						String name = null;
						CounterStyle style = null;
						for (Term<?> arg : f)
							if (name == null)
								if (arg instanceof TermIdent)
									name = ((TermIdent)arg).getValue();
								else
									throw new IllegalArgumentException(
										"invalid first argument of counter() function: should be the counter name");
							else if (style == null) {
								if (arg instanceof TermIdent)
									style = getNamedCounterStyle(((TermIdent)arg).getValue());
								else if (arg instanceof TermFunction)
									try {
										style = CounterStyle.fromSymbolsFunction((TermFunction)arg);
									} catch (IllegalArgumentException e) {
										throw new IllegalArgumentException(
											"invalid second argument of counter() function: should be a counter style", e);
									}
								else
									throw new IllegalArgumentException(
										"invalid second argument of counter() function: should be a counter style"); }
							else
								throw new IllegalArgumentException(
									"unexpected argument of counter() function: function takes at most two arguments");
						if (name == null)
							throw new IllegalArgumentException("counter() function requires at least one argument");
						if (style == null)
							style = CounterStyle.DECIMAL;
						s.append(evaluateCounter(name, style, false));
					} else if ("counters".equalsIgnoreCase(f.getFunctionName())) {
						String name = null;
						String separator = null;
						CounterStyle style = null;
						for (Term<?> arg : f)
							if (name == null)
								if (arg instanceof TermIdent)
									name = ((TermIdent)arg).getValue();
								else
									throw new IllegalArgumentException(
										"invalid first argument of counters() function: should be the counter name");
							else if (separator == null) {
								if (arg instanceof TermString)
									separator = ((TermString)arg).getValue();
								else
									throw new IllegalArgumentException(
										"invalid second argument of counters() function: should be a string"); }
							else if (style == null) {
								if (arg instanceof TermIdent)
									style = getNamedCounterStyle(((TermIdent)arg).getValue());
								else if (arg instanceof TermFunction)
									try {
										style = CounterStyle.fromSymbolsFunction((TermFunction)arg);
									} catch (IllegalArgumentException e) {
										throw new IllegalArgumentException(
											"invalid third argument of counters() function: should be a counter style", e);
									}
								else
									throw new IllegalArgumentException(
										"invalid third argument of counters() function: should be a counter style"); }
							else
								throw new IllegalArgumentException(
									"unexpected argument of counters() function: function takes at most three arguments");
						if (name == null || separator == null)
							throw new IllegalArgumentException("counters() function requires at least two arguments");
						if (style == null)
							style = CounterStyle.DECIMAL;
						s.append(evaluateCounters(name, style, separator));
					} else
						throw new RuntimeException(f.getFunctionName() + "() function not supported in content list"); // FIXME
				} else
					throw new IllegalStateException(); // cannot happen
			}
			return s.toString();
		}

		/**
		 * Evaluate marker contents (see https://www.w3.org/TR/css-lists-3/#content-property)
		 */
		private String generateMarkerContents(NodeData mainStyle, Map<PseudoElement,NodeData> pseudoStyles, Element element,
		                                      CounterStyle listCounterStyle) throws IllegalArgumentException {
			if (mainStyle.getProperty("display", false) == Display.LIST_ITEM) {
				for (PseudoElement pseudo : pseudoStyles.keySet()) {
					if ("marker".equals(pseudo.getName())) {
						NodeData pseudoStyle = pseudoStyles.get(pseudo);
						if (pseudoStyle.getProperty("content", false) == Content.content_list) {
							List<Term<?>> content = pseudoStyle.getValue(TermList.class, "content", false);
							if (content != null)
								return evaluateContent(content, element);
						}
						return null;
					}
				}
				if (listCounterStyle != null)
					return evaluateCounter("list-item", listCounterStyle, true);
			}
			return null;
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
