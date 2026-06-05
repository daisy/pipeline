package org.daisy.pipeline.common.saxon.impl;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.common.NormalizeLang;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "NormalizeLang",
	service = { ExtensionFunctionProvider.class }
)
public class NormalizeLangDefinition extends ReflexiveExtensionFunctionProvider {

	public NormalizeLangDefinition() {
		super(NormalizeLang.class);
	}
}
