package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.InlineStyle;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;
import org.daisy.pipeline.braille.css.impl.BrailleCssTreeBuilder.Style;

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
	private static final QName VENDOR_RULE = new QName("vendor-rule");
	
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
			SequenceType.OPTIONAL_STRING,
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
				boolean deep = arguments.length > 1
					? ((BooleanValue)arguments[1]).getBooleanValue()
					: false;
				Context styleCtxt = Context.ELEMENT; {
					if (arguments.length > 2) {
						Item i = arguments[2].head();
						if (i != null) {
							QName qn = ((QNameValue)i).toJaxpQName();
							if (qn.equals(PAGE))
								styleCtxt = Context.PAGE;
							else if (qn.equals(VOLUME))
								styleCtxt = Context.VOLUME;
							else if (qn.equals(VENDOR_RULE))
								styleCtxt = Context.VENDOR_RULE;
							else
								throw new RuntimeException(); }}}
				List<NodeInfo> result = new ArrayList<>();
				try {
					Style style = Style.of(new InlineStyle(arg.getStringValue(), styleCtxt));
					BrailleCssSerializer.toXml(
						style,
						new SaxonOutputValue(
							item -> {
								if (item instanceof XdmNode)
									result.add(((XdmNode)item).getUnderlyingNode());
								else
									throw new RuntimeException(); // should not happen
							},
							context.getConfiguration()
						).asXMLStreamWriter(),
						deep);
				} catch (Exception e) {
					logger.error("Error happened while parsing " + arg, e);
				}
				return new SequenceExtent(result);
			}
		};
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ParseStylesheetDefinition.class);
	
}
