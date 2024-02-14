package org.daisy.pipeline.braille.css;

import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.css.impl.BrailleCssParser;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Parse string to {@link SimpleInlineStyle} for use in {@link CSSStyledText}.
 */
public interface TextStyleParser {

	public SimpleInlineStyle parse(String style);

	public static TextStyleParser getInstance() {
		return BrailleCssParser.getInstance();
	}
}
