package org.daisy.dotify.impl.input;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.cr.InputManagerFactoryMakerService;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;

/**
 * Provides methods to access services in an SPI context without
 * breaking compatibility with OSGi. This class cannot be used in an OSGi context,
 * but it can be accessed from classes that are OSGi compatible if it is only
 * accessed when the runtime context is known not to be OSGi.
 * 
 * @author Joel Håkansson
 */
public class SPIHelper {
	private static InputManagerFactoryMakerService inputManagerFactory;
	private static PagedMediaWriterFactoryMakerService pagedMediaWriterFactory;
	private static FormatterEngineFactoryService formatterEngingeFactory;
	private final static Logger logger = Logger.getLogger(SPIHelper.class.getCanonicalName());
	
	/**
	 * <p>Gets a table catalog instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the table catalog implementation.</p>
	 * @return returns a table catalog
	 */
	public static InputManagerFactoryMakerService getInputManagerFactoryMakerService() {
		if (inputManagerFactory==null) {
			inputManagerFactory = (InputManagerFactoryMakerService)invoke("org.daisy.dotify.consumer.cr.InputManagerFactoryMaker");
		}
		return inputManagerFactory;
	}
	
	/**
	 * <p>Gets a table catalog instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the table catalog implementation.</p>
	 * @return returns a table catalog
	 */
	public static PagedMediaWriterFactoryMakerService getPagedMediaWriterFactoryMakerService() {
		if (pagedMediaWriterFactory==null) {
			pagedMediaWriterFactory = (PagedMediaWriterFactoryMakerService)invoke("org.daisy.dotify.consumer.writer.PagedMediaWriterFactoryMaker");
		}
		return pagedMediaWriterFactory;
	}
	
	public static FormatterEngineFactoryService getFormatterEngineFactoryService() {
		if (formatterEngingeFactory ==null) {
			Object o = invoke("org.daisy.dotify.consumer.engine.FormatterEngineMaker");
			try {
				Method m2 = o.getClass().getMethod("getFactory");
				formatterEngingeFactory = (FormatterEngineFactoryService)m2.invoke(o);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to invoke getFactory() with reflexion on: " + o.getClass(), e);
			}
		}
		return formatterEngingeFactory;
	}
	
	private static Object invoke(String className) {
		try {
			Class<?> cls = Class.forName(className);
			Method m = cls.getMethod("newInstance");
			return m.invoke(null);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to invoke newInstance() with reflexion on: " + className, e);
		}
		return null;
	}

}
