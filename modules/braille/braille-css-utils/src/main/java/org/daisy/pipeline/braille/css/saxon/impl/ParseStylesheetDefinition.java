package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import cz.vutbr.web.css.SupportedCSS;

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
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.QNameValue;
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
	
	private static final QName PAGE = new QName("page");
	private static final QName VOLUME = new QName("volume");
	private static final QName HYPHENATION_RESOURCE = new QName("hyphenation-resource");
	private static final QName TEXT_TRANSFORM = new QName("text-transform");
	private static final QName COUNTER_STYLE = new QName("counter-style");
	private static final QName VENDOR_RULE = new QName("vendor-rule");
	
	private static final SupportedCSS brailleCSS = new SupportedBrailleCSS(true, false);
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 1;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 3;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.OPTIONAL_ITEM,
			SequenceType.SINGLE_BOOLEAN,
			SequenceType.OPTIONAL_QNAME,
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
				boolean deep = arguments.length > 1
					? ((BooleanValue)arguments[1]).getBooleanValue()
					: false;
				boolean argIsAttr = false;
				Context styleCtxt = Context.ELEMENT; {
					if (arguments.length > 2 && arguments[2].head() != null) {
						QName qn = ((QNameValue)arguments[2].head()).toJaxpQName();
						if (qn.equals(PAGE))
							styleCtxt = Context.PAGE;
						else if (qn.equals(VOLUME))
							styleCtxt = Context.VOLUME;
						else if (qn.equals(HYPHENATION_RESOURCE))
							styleCtxt = Context.HYPHENATION_RESOURCE;
						else if (qn.equals(TEXT_TRANSFORM))
							styleCtxt = Context.TEXT_TRANSFORM;
						else if (qn.equals(COUNTER_STYLE))
							styleCtxt = Context.COUNTER_STYLE;
						else if (qn.equals(VENDOR_RULE))
							styleCtxt = Context.VENDOR_RULE;
						else
							throw new RuntimeException();
					} else if (arg instanceof NodeInfo && ((NodeInfo)arg).getNodeKind() == ATTRIBUTE) {
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
								style = name + ": " + style;
							}
						}
					}
				}
				BrailleCssStyle parsed = BrailleCssStyle.of(style, styleCtxt);
				if (argIsAttr && styleCtxt != Context.ELEMENT)
					parsed = BrailleCssStyle.of("@" + ((NodeInfo)arg).getLocalPart(), parsed);
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
					BrailleCssSerializer.toXml(parsed, writer, deep);
				} catch (Exception e) {
					logger.error("Error happened while parsing " + arg, e);
				}
				return new SequenceExtent(result);
			}
		};
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ParseStylesheetDefinition.class);
	
}
