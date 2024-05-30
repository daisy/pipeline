package org.daisy.pipeline.braille.css;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.pipeline.braille.css.impl.BrailleCssParser;

/**
 * Parse string to {@link SimpleInlineStyle} for use in {@link CSSStyledText}.
 */
public class TextStyleParser {

	public static SimpleInlineStyle parse(String style) {
		if (style == null) style = "";
		// clone because we make SimpleInlineStyle available and SimpleInlineStyle is mutable (and we want it to be)
		return BrailleCssParser.parseSimpleInlineStyle(style, null, true);
	}
}
