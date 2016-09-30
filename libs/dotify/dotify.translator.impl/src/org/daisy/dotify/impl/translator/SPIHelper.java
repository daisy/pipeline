package org.daisy.dotify.impl.translator;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;

/**
 * Provides methods to access services in an SPI context without
 * breaking compatibility with OSGi. This class cannot be used in an OSGi context,
 * but it can be accessed from classes that are OSGi compatible if it is only
 * accessed when the runtime context is known not to be OSGi.
 * 
 * @author Joel Håkansson
 */
public class SPIHelper {
	private static HyphenatorFactoryMakerService hyphenatorFactoryMaker;
	private final static Logger logger = Logger.getLogger(SPIHelper.class.getCanonicalName());
	
	/**
	 * <p>Gets a table catalog instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the table catalog implementation.</p>
	 * @return returns a table catalog
	 */
	public static HyphenatorFactoryMakerService getHyphenatorFactoryMakerService() {
		if (hyphenatorFactoryMaker==null) {
			hyphenatorFactoryMaker = (HyphenatorFactoryMakerService)invoke("org.daisy.dotify.consumer.hyphenator.HyphenatorFactoryMaker");
		}
		return hyphenatorFactoryMaker;
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
