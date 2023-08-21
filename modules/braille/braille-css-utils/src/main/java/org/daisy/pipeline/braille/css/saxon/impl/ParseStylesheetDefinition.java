package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.Optional;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
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

	private static final BrailleCssParser parser = BrailleCssParser.getInstance();

	public ParseStylesheetDefinition() {
		super(ParseStylesheet.class);
	}
	
	public static class ParseStylesheet {

		public static Optional<Style> parse(Optional<Object> style) {
			return parse(style.orElse(null), null, false);
		}

		public static Optional<Style> parse(Optional<Object> style, Optional<Style> parentStyle) {
			return parse(style.orElse(null), parentStyle.orElse(null), true);
		}

		private static Optional<Style> parse(Object style, Style parentStyle, boolean concretizeInherit) {
			String argStringValue;
			Attr attr = null;
			Element element = null;
			if (style == null)
				argStringValue = "";
			else if (style instanceof Attr) {
				attr = (Attr)style;
				argStringValue = attr.getNodeValue();
				element = (Element)attr.getParentNode();
			} else if (style instanceof Node) {
				argStringValue = ((Node)style).getTextContent();
			} else if (style instanceof String) {
				argStringValue = (String)style;
			} else
				throw new IllegalArgumentException("Unexpected type for first argument");
			if (parentStyle != null && !(parentStyle instanceof Stylesheet))
				throw new IllegalArgumentException("Unexpected type for second argument: " + parentStyle);
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
					} else if (parser.isSupportedCSSProperty(name)) {
						// assuming that context is a (pseudo-)element
						// assuming that the value is not "inherit"
						// not assuming that attr() and content() values have already been evaluated (although normally they will)
						Optional<cz.vutbr.web.css.Declaration> declaration
							= parser.parseDeclaration(name, argStringValue, element, false);
						if (declaration.isPresent())
							return Optional.of(new Declaration(declaration.get()));
						else
							return Optional.empty();
					}
				}
			}
			BrailleCssStyle s = concretizeInherit
				? parser.parseInlineStyle(argStringValue, styleCtxt, parentStyle != null ? ((Stylesheet)parentStyle).style : null)
				: parser.parseInlineStyle(argStringValue, styleCtxt);
			if (s.isEmpty())
				return Optional.empty();
			if (attr != null)
				if (styleCtxt == Context.ELEMENT)
					s = s.evaluate(element);
				else
					s = BrailleCssStyle.of("@" + attr.getLocalName(), s);
			return Optional.of(Stylesheet.of(s));
		}
	}
}
