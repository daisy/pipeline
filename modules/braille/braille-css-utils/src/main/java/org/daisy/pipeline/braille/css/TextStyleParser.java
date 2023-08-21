package org.daisy.pipeline.braille.css;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.css.impl.BrailleCssParser;

/**
 * Parse string to {@link SimpleInlineStyle} for use in {@link CSSStyledText}.
 */
public interface TextStyleParser {

	public SimpleInlineStyle parse(String style);

	/**
	 * Parse a style and inherit from a parent style. Using this method, rather than using {@link
	 * #parse(String)} and calling {@link SimpleInlineStyle#inheritFrom} on the result, allows for
	 * caching to happen.
	 *
	 * As opposed to {@link #parse(String)}, this method always concretizes "inherit" values (even
	 * if the provided parent style is {@code null} or empty).
	 */
	public SimpleInlineStyle parse(String style, SimpleInlineStyle parent);

	public static TextStyleParser getInstance() {
		return BrailleCssParser.getInstance();
	}
}
