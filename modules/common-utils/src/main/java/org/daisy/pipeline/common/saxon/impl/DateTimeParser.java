package org.daisy.pipeline.common.saxon.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

public abstract class DateTimeParser {
	private DateTimeParser() {}

	public static Date parse(String input, String format) throws ParseException {
		return new SimpleDateFormat(format).parse(input);
	}

	@Component(
		name = "DateTimeParser",
		service = { ExtensionFunctionProvider.class }
	)
	public static class FunctionProvider extends ReflexiveExtensionFunctionProvider {

		public FunctionProvider() {
			super(DateTimeParser.class);
		}
	}
}
