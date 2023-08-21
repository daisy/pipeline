package org.daisy.pipeline.braille.css;

import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.pipeline.braille.css.impl.StyleTransformerImpl;
import org.daisy.pipeline.braille.css.xpath.Style;

/**
 * Transform a style to a different model.
 */
public interface StyleTransformer {

	/**
	 * @param fromSupportedBrailleCSS input model
	 * @param toSupportedBrailleCSS output model
	 */
	public static StyleTransformer of(SupportedBrailleCSS fromSupportedBrailleCSS,
	                                  SupportedBrailleCSS toSupportedBrailleCSS) {
		return new StyleTransformerImpl(fromSupportedBrailleCSS, toSupportedBrailleCSS);
	}

	public SimpleInlineStyle transform(SimpleInlineStyle style);

	public Style transform(Style style);

}
