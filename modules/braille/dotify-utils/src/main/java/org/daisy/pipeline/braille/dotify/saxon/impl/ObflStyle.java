package org.daisy.pipeline.braille.dotify.saxon.impl;

import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.braille.css.StyleTransformer;
import org.daisy.pipeline.braille.css.TextStyleParser;
import org.daisy.pipeline.braille.css.xpath.Style;
import org.daisy.pipeline.braille.dotify.impl.SupportedOBFLProperties;

import org.osgi.service.component.annotations.Component;

public class ObflStyle {

	public Style of(Style style) {
		return transformer.transform(style);
	}

	private final StyleTransformer transformer;

	private ObflStyle(StyleTransformer transformer) {
		this.transformer = transformer;
	}

	@Component(
		name = "ObflStyle",
		service = { ExtensionFunctionProvider.class }
	)
	public static class ObflStyleFunctionProvider extends ReflexiveExtensionFunctionProvider {

		private static final SupportedBrailleCSS brailleCSS
			// using a hack to get hold of SupportedBrailleCSS because BrailleCssParser is not visiblej
			= TextStyleParser.getInstance().parse("display: block").iterator().next().getSupportedBrailleCSS();

		public ObflStyleFunctionProvider() {
			super();
			addExtensionFunctionDefinitionsFromClass(
				ObflStyle.class,
				new ObflStyle(
					StyleTransformer.of(
						brailleCSS,
						new SupportedOBFLProperties(false, true, "-obfl-"))));
		}
	}
}
