package org.daisy.dotify.tasks.impl.input;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;

/**
 * Provides methods to access services in an SPI context without
 * breaking compatibility with OSGi. This class cannot be used in an OSGi context,
 * but it can be accessed from classes that are OSGi compatible if it is only
 * accessed when the runtime context is known not to be OSGi.
 * 
 * @author Joel Håkansson
 */
public class SPIHelper {
	private static TaskGroupFactoryMakerService inputManagerFactory;
	private static final Logger logger = Logger.getLogger(SPIHelper.class.getCanonicalName());
	
	private SPIHelper() {}

	/**
	 * <p>Gets a table catalog instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the table catalog implementation.</p>
	 * @return returns a table catalog
	 */
	public static TaskGroupFactoryMakerService getInputManagerFactoryMakerService() {
		if (inputManagerFactory==null) {
			inputManagerFactory = (TaskGroupFactoryMakerService)invoke("org.daisy.dotify.consumer.tasks.TaskGroupFactoryMaker");
		}
		return inputManagerFactory;
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
