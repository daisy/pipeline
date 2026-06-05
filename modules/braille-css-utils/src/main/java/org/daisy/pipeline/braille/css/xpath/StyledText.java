package org.daisy.pipeline.braille.css.xpath;

import java.util.Collections;
import java.util.IllformedLocaleException;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;
import org.daisy.pipeline.braille.css.TextStyleParser;
import org.daisy.pipeline.braille.css.xpath.impl.Stylesheet;

public class StyledText {

	private final TextStyleParser textStyleParser;

	public StyledText(TextStyleParser textStyleParser) {
		this.textStyleParser = textStyleParser;
	}

	public static String getText(CSSStyledText t) {
		return t.getText();
	}

	/**
	 * @param t assumed to not change
	 */
	public static Optional<Style> getStyle(CSSStyledText t) {
		SimpleInlineStyle s = t.getStyle();
		if (s == null || s.isEmpty())
			return Optional.empty();
		return Optional.of(Stylesheet.of(BrailleCssStyle.of(s)));
	}

	public static Optional<String> getLanguage(CSSStyledText t) {
		Locale l = t.getLanguage();
		if (l == null)
			return Optional.empty();
		return Optional.of(l.toLanguageTag());
	}

	public static Map<String,String> getTextAttributes(CSSStyledText t) {
		Map<String,String> map = t.getTextAttributes();
		return map != null ? map : Collections.emptyMap();
	}

	public CSSStyledText of(String text) {
		return of(text, Optional.empty());
	}

	public CSSStyledText of(String text, Optional<Object> style) {
		return of(text, style, Optional.empty());
	}

	public CSSStyledText of(String text, Optional<Object> style, Optional<String> language) {
		return of(text, style, language, null);
	}

	public CSSStyledText of(String text, Optional<Object> style, Optional<String> language, Map<String,String> textAttributes) {
		Locale l = null; {
			if (language != null && language.isPresent()) {
				try {
					l = (new Locale.Builder()).setLanguageTag(language.get().replace('_','-')).build();
				} catch (IllformedLocaleException e) {
					throw new IllegalArgumentException("Locale '" + language.get() + "' could not be parsed", e);
				}
			}
		}
		if (style != null && style.isPresent()) {
			Object o = style.get();
			if (o instanceof Style) {
				SimpleInlineStyle s; {
					try {
						if (!(o instanceof Stylesheet))
							throw new IllegalArgumentException("Unexpected style argument");
						if (((Stylesheet)o).style == null)
							s = SimpleInlineStyle.EMPTY;
						else
							s = ((Stylesheet)o).style.asSimpleInlineStyle(true); // this will raise a UnsupportedOperationException
							                                                     // when the provided style is not a simple style
					} catch (UnsupportedOperationException e) {
						throw new IllegalArgumentException("Unexpected style argument");
					}
				}
				return new CSSStyledText(text, s, l, textAttributes);
			} else if (o instanceof String) {
				String s = (String)o;
				return new CSSStyledText(text, textStyleParser.parse(s), l, textAttributes);
			} else
				throw new IllegalArgumentException("Unexpected style argument: expected `Style` object or string");
		} else
			return new CSSStyledText(text, null, l, textAttributes);
	}
}
