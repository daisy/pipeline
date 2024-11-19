package org.daisy.pipeline.braille.css.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.PropertyValue;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser.ParsedDeclaration;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser.ParsedDeclarations;
import org.daisy.pipeline.braille.css.StyleTransformer;
import org.daisy.pipeline.braille.css.xpath.Style;
import org.daisy.pipeline.braille.css.xpath.impl.Stylesheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StyleTransformerImpl implements StyleTransformer {

	private final static Logger logger = LoggerFactory.getLogger(StyleTransformer.class);

	private final SupportedBrailleCSS fromSupportedBrailleCSS;
	private final SupportedBrailleCSS toSupportedBrailleCSS;
	private final BrailleCssParser parser;
	private final Set<String> propertiesWithDifferentDefault;

	public StyleTransformerImpl(SupportedBrailleCSS fromSupportedBrailleCSS,
	                            SupportedBrailleCSS toSupportedBrailleCSS) {
		this.fromSupportedBrailleCSS = fromSupportedBrailleCSS;
		this.toSupportedBrailleCSS = toSupportedBrailleCSS;
		this.parser = new BrailleCssParser() {
				@Override
				public BrailleCSSParserFactory getBrailleCSSParserFactory() {
					return null; }; // should be fine to return null (assuming parseInlineStyle() will not
					                // be called on this parser object)
				@Override
				public Optional<SupportedBrailleCSS> getSupportedBrailleCSS(Context context) {
					return Optional.of(toSupportedBrailleCSS); }};
		this.propertiesWithDifferentDefault = new HashSet<>(); {
			for (String p : toSupportedBrailleCSS.getDefinedPropertyNames())
				if (fromSupportedBrailleCSS.isSupportedCSSProperty(p))
					if ("".equals(fromSupportedBrailleCSS.getDefaultProperty(p).toString())
					    ? (Objects.equals(fromSupportedBrailleCSS.getDefaultProperty(p),
					                      toSupportedBrailleCSS.getDefaultProperty(p)) &&
					       Objects.equals(fromSupportedBrailleCSS.getDefaultValue(p),
					                      toSupportedBrailleCSS.getDefaultValue(p)))
					    : Objects.equals(fromSupportedBrailleCSS.getDefaultProperty(p).toString(),
					                     toSupportedBrailleCSS.getDefaultProperty(p).toString()))
						; // same default
					else
						propertiesWithDifferentDefault.add(p);
		}
	}

	@Override
	public final SimpleInlineStyle transform(SimpleInlineStyle style) {
		Map<String,CSSProperty> properties = new HashMap<>();
		Map<String,Term<?>> values = new HashMap<>();
		Map<String,Declaration> sourceDeclarations = new HashMap<>();
		List<ParsedDeclaration> declarations = new ArrayList<>();
		for (PropertyValue d : style) {
			if (d.getSupportedBrailleCSS() != fromSupportedBrailleCSS)
				throw new IllegalArgumentException();
			String p = d.getProperty();
			if (!toSupportedBrailleCSS.isSupportedCSSProperty(p)) {
				// ignore "inherit" (if style is based on parent, inherit has already been concretized;
				//                 if not based on parent, this is equivalent to "initial")
				if (d.getCSSProperty().equalsInherit())
					;
				// ignore "initial" (should already have been concretized; properties with different
				//                   default in the output model are handled below)
				else if (d.getCSSProperty().equalsInitial())
					;
				// ignore values that are the default (equivalent to "initial")
				else if (Objects.equals(d.getCSSProperty(), fromSupportedBrailleCSS.getDefaultProperty(p)) &&
				         Objects.equals(d.getValue(), fromSupportedBrailleCSS.getDefaultValue(p)))
					;
				else
					logger.warn("Property '{}' not supported", p);
			} else if (!toSupportedBrailleCSS.parseDeclaration(d, properties, values))
				logger.warn("Property '{}' not supported", d);
			else
				sourceDeclarations.put(p, d.getSourceDeclaration());
		}
		for (String p : toSupportedBrailleCSS.getDefinedPropertyNames()) {
			CSSProperty property = null;
			Term<?> value = null;
			if (properties.containsKey(p))
				declarations.add(new ParsedDeclaration(parser,
				                                       Context.ELEMENT,
				                                       p,
				                                       properties.get(p),
				                                       values.get(p),
				                                       sourceDeclarations.get(p)));
			else if (propertiesWithDifferentDefault.contains(p))
				declarations.add(new ParsedDeclaration(parser,
				                                       Context.ELEMENT,
				                                       p,
				                                       toSupportedBrailleCSS.getDefaultProperty(p),
				                                       toSupportedBrailleCSS.getDefaultValue(p),
				                                       null));
		}
		return new ParsedDeclarations(parser, Context.ELEMENT, declarations);
	}

	@Override
	public final Style transform(Style style) {
		if (!(style instanceof Stylesheet))
			throw new IllegalArgumentException();
		Stylesheet fs = (Stylesheet)style;
		SimpleInlineStyle s = fs.style != null
			? fs.style.asSimpleInlineStyle(false)
			: SimpleInlineStyle.EMPTY;
		s = transform(s);
		return Stylesheet.of(BrailleCssStyle.of(s));
	}
}
