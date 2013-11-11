package org.daisy.dotify.consumer.engine;

import javax.imageio.spi.ServiceRegistry;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.consumer.formatter.FormatterFactoryMaker;
import org.daisy.dotify.consumer.obfl.ExpressionFactoryMaker;
import org.daisy.dotify.consumer.translator.MarkerProcessorFactoryMaker;
import org.daisy.dotify.consumer.translator.TextBorderFactoryMaker;

public class FormatterEngineMaker {
	private final FormatterEngineFactoryService proxy;

	public FormatterEngineMaker() {
		// Gets the first formatter engine (assumes there is at least one).
		proxy = ServiceRegistry.lookupProviders(FormatterEngineFactoryService.class).next();
		// populate the engine factory with SPI here as this class is never used
		// from OSGi
		proxy.setFormatterFactory(FormatterFactoryMaker.newInstance().getFactory());
		proxy.setMarkerProcessor(MarkerProcessorFactoryMaker.newInstance());
		proxy.setTextBorderFactoryMaker(TextBorderFactoryMaker.newInstance());
		proxy.setExpressionFactory(ExpressionFactoryMaker.newInstance().getFactory());
	}

	public static FormatterEngineMaker newInstance() {
		return new FormatterEngineMaker();
	}

	public FormatterEngine newFormatterEngine(String locale, String mode, PagedMediaWriter writer) {
		return proxy.newFormatterEngine(locale, mode, writer);
	}

}
