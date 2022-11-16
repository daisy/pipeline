package org.daisy.pipeline.braille.css.saxon.impl;

import javax.xml.namespace.QName;

import cz.vutbr.web.css.SupportedCSS;

import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import static net.sf.saxon.type.Type.ATTRIBUTE;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.xpath.impl.Declaration;
import org.daisy.pipeline.braille.css.xpath.impl.Stylesheet;
import org.daisy.pipeline.braille.css.xpath.Style;

import org.osgi.service.component.annotations.Component;

import org.w3c.dom.Element;

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
		return SequenceType.SINGLE_ITEM; // ObjectValue
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				if (arguments.length == 0)
					return EMPTY;
				Item arg = arguments[0].head();
				if (arg == null)
					return EMPTY;
				String argStringValue = arg.getStringValue();
				boolean argIsAttr = (arg instanceof NodeInfo && ((NodeInfo)arg).getNodeKind() == ATTRIBUTE);
				QName attrName = argIsAttr
					? new QName(((NodeInfo)arg).getURI(), ((NodeInfo)arg).getLocalPart())
					: null;
				Element parentElement = argIsAttr
					?  (ElementOverNodeInfo)ElementOverNodeInfo.wrap(((NodeInfo)arg).getParent())
					: null;
				return new ObjectValue<>(parse(argStringValue, argIsAttr, attrName, parentElement));
			}
		};
	}

	private static Style parse(String argStringValue, boolean argIsAttr, QName attrName, Element context) {
		Context styleCtxt = Context.ELEMENT;
		String styleAsString = argStringValue;
		boolean returnSingleProperty = false;
		if (argIsAttr) {
			if (XMLNS_CSS.equals(attrName.getNamespaceURI())) {
				String name = attrName.getLocalPart().replaceAll("^_", "-");
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
					returnSingleProperty = true;
					styleAsString = name + ": " + argStringValue;
				}
			}
		}
		BrailleCssStyle style = BrailleCssStyle.of(styleAsString, styleCtxt);
		if (argIsAttr)
			if (styleCtxt == Context.ELEMENT)
				style = style.evaluate(context);
			else
				style = BrailleCssStyle.of("@" + attrName.getLocalPart(), style);
		if (returnSingleProperty)
			return new Declaration(style);
		else
			return new Stylesheet(style);
	}

	private static final ObjectValue<Style> EMPTY = new ObjectValue<>(Stylesheet.EMPTY);

}
