package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.Optional;

import cz.vutbr.web.css.SupportedCSS;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.xpath.impl.Declaration;
import org.daisy.pipeline.braille.css.xpath.impl.Stylesheet;
import org.daisy.pipeline.braille.css.xpath.Style;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Component(
	name = "ParseStylesheet",
	service = { ExtensionFunctionProvider.class }
)
public class ParseStylesheetDefinition extends ReflexiveExtensionFunctionProvider {

	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";

	private static final SupportedCSS brailleCSS = new SupportedBrailleCSS(true, false);

	public ParseStylesheetDefinition() {
		super(ParseStylesheet.class);
	}
	
	public static class ParseStylesheet {

		public static Style parse(Optional<Object> style) {
			return parse(style.orElse(null));
		}

		private static Style parse(Object style) {
			if (style == null)
				return Stylesheet.EMPTY;
			String argStringValue;
			Attr attr = null;
			Element element = null;
			if (style instanceof Attr) {
				attr = (Attr)style;
				argStringValue = attr.getNodeValue();
				element = (Element)attr.getParentNode();
			} else if (style instanceof Node) {
				argStringValue = ((Node)style).getTextContent();
			} else if (style instanceof String) {
				argStringValue = (String)style;
			} else
				throw new IllegalArgumentException("Unexpected type for first argument");
			Context styleCtxt = Context.ELEMENT;
			if (attr != null) {
				if (XMLNS_CSS.equals(attr.getNamespaceURI())) {
					String name = attr.getLocalName().replaceAll("^_", "-");
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
						// assuming that context is a (pseudo-)element
						// assuming that the value is not "inherit"
						// not assuming that attr() values have already been evaluated (although normally they will)
						Optional<cz.vutbr.web.css.Declaration> declaration
							= BrailleCssParser.parseDeclaration(name, argStringValue, element, false);
						if (declaration.isPresent())
							return new Declaration(declaration.get());
						else
							return Declaration.EMPTY;
					}
				}
			}
			BrailleCssStyle s = BrailleCssStyle.of(argStringValue, styleCtxt);
			if (s.isEmpty())
				return Stylesheet.EMPTY;
			if (attr != null)
				if (styleCtxt == Context.ELEMENT)
					s = s.evaluate(element);
				else
					s = BrailleCssStyle.of("@" + attr.getLocalName(), s);
			return new Stylesheet(s);
		}
	}
}
