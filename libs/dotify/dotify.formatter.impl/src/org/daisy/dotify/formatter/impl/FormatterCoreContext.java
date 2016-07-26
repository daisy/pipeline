package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;

class FormatterCoreContext {
	private final TextBorderFactoryMakerService tbf;
	private final FormatterConfiguration config;
	private final MarkerProcessorFactoryMakerService mpf;

	FormatterCoreContext(TextBorderFactoryMakerService tbf, FormatterConfiguration config, MarkerProcessorFactoryMakerService mpf) {
		this.tbf = tbf;
		this.config = config;
		this.mpf = mpf;
	}

	String getTranslatorMode() {
		return config.getTranslationMode();
	}

	TextBorderFactoryMakerService getTextBorderFactoryMakerService() {
		return tbf;
	}
	
	FormatterConfiguration getConfiguration() {
		return config;
	}

	MarkerProcessorFactoryMakerService getMarkerProcessorFactoryMakerService() {
		return mpf;
	}
}
