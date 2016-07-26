package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;

class LazyFormatterContext {
	private final BrailleTranslatorFactoryMakerService translatorFactory;
	private final TextBorderFactoryMakerService tbf;
	private final MarkerProcessorFactoryMakerService mpf;
	private FormatterContext context = null;
	private FormatterConfiguration config = null;

	LazyFormatterContext(BrailleTranslatorFactoryMakerService translatorFactory, TextBorderFactoryMakerService tbf, MarkerProcessorFactoryMakerService mpf, FormatterConfiguration config) {
		if (config==null) {
			throw new IllegalArgumentException();
		}
		this.translatorFactory = translatorFactory;
		this.tbf = tbf;
		this.mpf = mpf;
		this.config = config;
	}
	
	synchronized FormatterContext getFormatterContext() {
		if (context==null) {
			context = new FormatterContext(translatorFactory, tbf, mpf, config);
		}
		return context;
	}

	void setConfiguration(FormatterConfiguration config) {
		if (config==null) {
			throw new IllegalArgumentException();
		}
		context = null;
		this.config = config;
	}

}
