package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import cz.vutbr.web.css.SupportedCSS;

import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import static net.sf.saxon.type.Type.ATTRIBUTE;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "css:parse-stylesheet",
	service = { ExtensionFunctionDefinition.class }
)
public class ParseStylesheetDefinition extends ExtensionFunctionDefinition {
	
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	
	private static final StructuredQName funcname = new StructuredQName("css", XMLNS_CSS, "parse-stylesheet");
	
	private static final SupportedCSS brailleCSS = new SupportedBrailleCSS(true, false);
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.OPTIONAL_ITEM
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.NODE_SEQUENCE;
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				if (arguments.length == 0)
					return EmptySequence.getInstance();
				Item arg = arguments[0].head();
				if (arg == null)
					return EmptySequence.getInstance();
				String style = arg.getStringValue();
				boolean argIsAttr = false;
				boolean argIsPropertyAttr = false;
				Context styleCtxt = Context.ELEMENT; {
					if (arg instanceof NodeInfo && ((NodeInfo)arg).getNodeKind() == ATTRIBUTE) {
						argIsAttr = true;
						if (XMLNS_CSS.equals(((NodeInfo)arg).getURI())) {
							String name = ((NodeInfo)arg).getLocalPart().replaceAll("^_", "-");
							if ("page".equals(name)) {
								styleCtxt = Context.PAGE;
							} else if ("volume".equals(name)) {
								styleCtxt = Context.VOLUME;
							} else if ("hyphenation-resource".equals(name)) {
								styleCtxt = Context.HYPHENATION_RESOURCE;
							} else if ("text-transform".equals(name)) {
								styleCtxt = Context.TEXT_TRANSFORM;
							} else if ("counter-style".equals(name)) {
								styleCtxt = Context.COUNTER_STYLE;
							} else if (brailleCSS.isSupportedCSSProperty(name) || name.startsWith("-")) {
								argIsPropertyAttr = true;
								style = name + ": " + style;
							}
						}
					}
				}
				BrailleCssStyle parsed = BrailleCssStyle.of(style, styleCtxt);
				if (argIsAttr)
					if (styleCtxt != Context.ELEMENT)
						parsed = BrailleCssStyle.of("@" + ((NodeInfo)arg).getLocalPart(), parsed);
					else
						parsed = parsed.evaluate((ElementOverNodeInfo)ElementOverNodeInfo.wrap(((NodeInfo)arg).getParent()));
				List<NodeInfo> result = new ArrayList<>();
				try {
					XMLStreamWriter writer = new SaxonOutputValue(
						item -> {
							if (item instanceof XdmNode)
								result.add(((XdmNode)item).getUnderlyingNode());
							else
								throw new RuntimeException(); // should not happen
						},
						context.getConfiguration()
					).asXMLStreamWriter();
					if (argIsPropertyAttr)
						// <css:property>
						BrailleCssSerializer.toXml(parsed, writer, true);
					else
						// <css:rule>
						BrailleCssSerializer.toXml(parsed, writer);
				} catch (Exception e) {
					logger.error("Error happened while parsing " + arg, e);
				}
				return new SequenceExtent(result);
			}
		};
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ParseStylesheetDefinition.class);
	
}
