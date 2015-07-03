package org.daisy.dotify.consumer.engine;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineConfigurationException;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.consumer.formatter.FormatterFactoryMaker;
import org.daisy.dotify.consumer.obfl.ExpressionFactoryMaker;
import org.daisy.dotify.consumer.translator.MarkerProcessorFactoryMaker;
import org.daisy.dotify.consumer.translator.TextBorderFactoryMaker;

/**
 * <p>Like all classes in the org.daisy.dotify.consumer sub packages, this
 * class is only used directly in SPI context. Unlike some other classes however,
 * this class does not implement a service interface that can be used from
 * OSGi. The reason for this is that the implementation <i>simply returns
 * a single instance of the lower level interface</i> with references populated
 * with SPI. To use in OSGi context, request the lower level service directly
 * from the DS registry.</p>
 * 
 * @author Joel Håkansson
 */
public class FormatterEngineMaker {
	private final FormatterEngineFactoryService proxy;

	public FormatterEngineMaker() {
		// Gets the first formatter engine (assumes there is at least one).
		proxy = ServiceLoader.load(FormatterEngineFactoryService.class).iterator().next();
		// populate the engine factory with SPI here as this class is never used
		// from OSGi
		setReference(FormatterFactory.class, FormatterFactoryMaker.newInstance().getFactory());
		setReference(MarkerProcessorFactoryMakerService.class, MarkerProcessorFactoryMaker.newInstance());
		setReference(TextBorderFactoryMakerService.class, TextBorderFactoryMaker.newInstance());
		setReference(ExpressionFactory.class, ExpressionFactoryMaker.newInstance().getFactory());
	}
	
	private <T> void setReference(Class<T> c, T ref) {
		try {
			proxy.setReference(c, ref);
		} catch (FormatterEngineConfigurationException e) {
			Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Failed to set reference.", e);
		}
	}
	
	public FormatterEngineFactoryService getFactory() {
		return proxy;
	}

	public static FormatterEngineMaker newInstance() {
		return new FormatterEngineMaker();
	}

	public FormatterEngine newFormatterEngine(String locale, String mode, PagedMediaWriter writer) {
		return proxy.newFormatterEngine(locale, mode, writer);
	}

}
