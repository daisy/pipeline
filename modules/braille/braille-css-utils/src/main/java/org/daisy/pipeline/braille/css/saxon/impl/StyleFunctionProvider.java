package org.daisy.pipeline.braille.css.saxon.impl;

import org.daisy.pipeline.braille.css.xpath.Style;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "Style",
	service = { ExtensionFunctionProvider.class }
)
public class StyleFunctionProvider extends ReflexiveExtensionFunctionProvider {

	public StyleFunctionProvider() {
		super(Style.class);
	}
}
