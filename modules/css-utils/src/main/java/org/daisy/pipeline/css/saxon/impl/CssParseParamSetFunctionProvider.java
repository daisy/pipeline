package org.daisy.pipeline.css.saxon.impl;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.css.impl.StylesheetParametersParser;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:css-parse-param-set",
	service = { ExtensionFunctionProvider.class }
)
public class CssParseParamSetFunctionProvider extends ReflexiveExtensionFunctionProvider {

	public CssParseParamSetFunctionProvider() {
		super(StylesheetParametersParser.class);
	}
}
