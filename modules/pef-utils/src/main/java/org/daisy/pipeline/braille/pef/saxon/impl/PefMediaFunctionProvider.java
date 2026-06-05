package org.daisy.pipeline.braille.pef.saxon.impl;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.pipeline.css.Medium;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pf:pef-assert-embossable",
	service = { ExtensionFunctionProvider.class }
)
public class PefMediaFunctionProvider extends ReflexiveExtensionFunctionProvider {

	public PefMediaFunctionProvider() {
		super(PefMediaFunctions.class);
	}

	public static class PefMediaFunctions {

		// pf:pef-assert-embossable
		public static Medium assertEmbossable(Medium medium) throws AssertionError {
			if (medium == null || medium.getType() != Medium.Type.EMBOSSED)
				throw new AssertionError(
					"Can not convert to medium: expected type 'embossed', but got: " + medium, null);
			if (!(medium instanceof FileFormat))
				throw new AssertionError(
					"Can not convert to medium: expected a file format, but got: " + medium, null);
			return medium;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(PefMediaFunctions.class);

}
