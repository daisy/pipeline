package org.daisy.dotify.consumer.engine;

import java.util.ServiceLoader;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.writer.PagedMediaWriter;

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
//TODO: deprecate or add service layer
//See: https://github.com/joeha480/dotify/issues/161
public class FormatterEngineMaker {
	private final FormatterEngineFactoryService proxy;

	public FormatterEngineMaker() {
		// Gets the first formatter engine (assumes there is at least one).
		proxy = ServiceLoader.load(FormatterEngineFactoryService.class).iterator().next();
		proxy.setCreatedWithSPI();
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
