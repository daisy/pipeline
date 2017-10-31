package org.daisy.dotify.tasks.impl.input;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Provides methods to access services in an SPI context without
 * breaking compatibility with OSGi. This class cannot be used in an OSGi context,
 * but it can be accessed from classes that are OSGi compatible if it is only
 * accessed when the runtime context is known not to be OSGi.
 * 
 * @author Joel Håkansson
 */
public class SPIHelper {
	private static final Logger logger = Logger.getLogger(SPIHelper.class.getCanonicalName());
	
	private SPIHelper() {}

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
