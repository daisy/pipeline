package org.daisy.pipeline.braille.css.saxon.impl;

import org.daisy.pipeline.braille.css.TextStyleParser;
import org.daisy.pipeline.braille.css.xpath.StyledText;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "StyledText",
	service = { ExtensionFunctionProvider.class }
)
public class StyledTextFunctionProvider extends ReflexiveExtensionFunctionProvider {

	public StyledTextFunctionProvider() {
		addExtensionFunctionDefinitionsFromClass(StyledText.class, new StyledText(TextStyleParser.getInstance()));
	}
}
