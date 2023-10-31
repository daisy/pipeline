package org.daisy.pipeline.css.speech.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.URIResolver;

import cz.vutbr.web.css.CSSProperty.Content;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.csskit.antlr.CSSParserFactory;
import cz.vutbr.web.csskit.RuleFactoryImpl;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.SupportedCSS21;

import org.daisy.common.file.URLs;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.pipeline.css.CssCascader;
import org.daisy.pipeline.css.CssPreProcessor;
import org.daisy.pipeline.css.JStyleParserCssCascader;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.XsltProcessor;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

@Component(
	name = "SpeechCssCascader",
	service = { CssCascader.class }
)
public class SpeechCssCascader implements CssCascader {

	public boolean supportsMedium(Medium medium) {
		switch (medium.getType()) {
		case SPEECH:
			return true;
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
		if (!multipleAttrs)
			throw new UnsupportedOperationException("Cascading to single attribute per element not supported");
		switch (medium.getType()) {
		case SPEECH:
			return new Transformer(uriResolver, preProcessor, xsltProcessor, userStylesheet, medium, attributeName);
		default:
			throw new IllegalArgumentException("medium not supported: " + medium);
		}
	}

	private static final SupportedCSS speechCSS = SupportedCSS21.getInstance();
	private static final DeclarationTransformer declarationTransformer = new SpeechDeclarationTransformer();
	private static final RuleFactory ruleFactory = RuleFactoryImpl.getInstance();
	private static final CSSParserFactory parserFactory = CSSParserFactory.getInstance();
	private static final Set<String> speechCSSProperties = new HashSet<>(
		Arrays.asList(
			"voice-family", "stress", "richness", "cue", "cue-before", "cue-after", "pause",
			"pause-after", "pause-before", "azimuth", "volume", "speak", "play-during",
			"elevation", "speech-rate", "pitch", "pitch-range", "stress", "speak-punctuation",
			"speak-numeral", "speak-header"));

	private static class Transformer extends JStyleParserCssCascader {

		private final String attributePrefix;
		private final String attributeNamespaceURI;

		private Transformer(URIResolver resolver, CssPreProcessor preProcessor, XsltProcessor xsltProcessor,
		                    String userStyleSheet, Medium medium, QName attributeNamespace) {
			super(resolver, preProcessor, xsltProcessor, userStyleSheet, medium, null,
			      parserFactory, ruleFactory, speechCSS, declarationTransformer);
			this.attributePrefix = attributeNamespace.getPrefix();
			this.attributeNamespaceURI = attributeNamespace.getNamespaceURI();
		}

		protected Map<QName,String> serializeStyle(NodeData mainStyle, Map<PseudoElement,NodeData> pseudoStyles, Element context) {
			Map<QName,String> style = null;
			if (mainStyle != null) {
				for (String property : mainStyle.getPropertyNames()) {
					if (speechCSSProperties.contains(property)) {
						String s; {
							Term<?> v = mainStyle.getValue(property, false);
							if (v == null || v.getValue() == null) {
								// jStyleParser replaces '-' with '_'. Best workaround so far is to
								// do the opposite: (voice-family and cue aside, there is no
								// property values with '_' in Aural CSS)
								s = mainStyle.getProperty(property, false).toString().replace("_", "-").toLowerCase();
							} else if (v.getValue() instanceof List<?>) {
								StringBuilder b = new StringBuilder();
								Iterator<?> i = ((List<?>)v.getValue()).iterator();
								b.append(i.next());
								while (i.hasNext())
									b.append("," + ((Term<?>)i.next()).getValue().toString());
								s = b.toString();
							} else if (v instanceof TermURI) {
								URI uri = URLs.asURI(((TermURI)v).getValue());
								if (!uri.isAbsolute() && !uri.getSchemeSpecificPart().startsWith("/")) {
									// relative URI: try to make absolute
									if (((TermURI)v).getBase() != null)
										uri = URLs.resolve(URLs.asURI(((TermURI)v).getBase()), uri);
								}
								s = uri.toASCIIString();
							} else {
								s = v.toString().replace("_", "-").toLowerCase();
							}
						}
						if (style == null)
							style = new HashMap<>();
						style.put(new QName(attributeNamespaceURI, property, attributePrefix), s);
					}
				}
			}
			// process ::after and ::before pseudo elements
			for (PseudoElement pseudo : pseudoStyles.keySet()) {
				if ("::after".equals(pseudo.toString()) || "::before".equals(pseudo.toString())) {
					NodeData nd = pseudoStyles.get(pseudo);
					if (nd != null) {
						for (String property : nd.getPropertyNames()) {
							if ("content".equals(property)) {
								switch ((Content)nd.getProperty(property)) {
								case list_values:
									StringBuilder value = new StringBuilder();
									for (Term<?> t : (TermList)nd.getValue(property, false)) {
										if (t instanceof TermString) {
											value.append(t.getValue());
											continue;
										} else if (t instanceof TermFunction) {
											TermFunction f = (TermFunction)t;
											if ("attr".equals(f.getFunctionName().toLowerCase())
											    && f.size() == 1
											    && f.get(0) instanceof TermIdent) {
												value.append(context.getAttribute(((TermIdent)f.get(0)).getValue()));
												continue;
											}
										}
										logger.warn("Don't know how to speak content value " + t
										            + " within ::" + pseudo.getName() + " pseudo-element");
										value = null;
										break;
									}
									if (value != null && value.length() > 0) {
										if (style == null)
											style = new HashMap<>();
										style.put(new QName(attributeNamespaceURI, pseudo.getName(), attributePrefix), value.toString());
									}
									break;
								case NORMAL:
								case NONE:
								case INHERIT:
								case INITIAL:
								default:
									break;
								}
								break;
							} else if (speechCSSProperties.contains(property)) {
								logger.warn("Ignoring property '"+ property
								            + "' within ::" + pseudo.getName() + " pseudo-element");
							}
						}
					}
				}
			}
			return style;
		}

		protected String serializeValue(Term<?> value) {
			throw new UnsupportedOperationException();
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(SpeechCssCascader.class);

}
