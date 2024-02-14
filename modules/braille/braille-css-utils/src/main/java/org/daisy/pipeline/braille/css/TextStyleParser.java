package org.daisy.pipeline.braille.css;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.css.impl.BrailleCssParser;

/**
 * Parse string to {@link SimpleInlineStyle} for use in {@link CSSStyledText}.
 */
public interface TextStyleParser {

	public SimpleInlineStyle parse(String style);

	public static TextStyleParser getInstance() {
		return BrailleCssParser.getInstance();
	}
}
