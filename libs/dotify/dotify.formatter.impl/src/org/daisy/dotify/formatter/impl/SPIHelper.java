package org.daisy.dotify.formatter.impl;

import java.lang.reflect.Method;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;

/**
 * Provides methods to access services in an SPI context without
 * breaking compatibility with OSGi. This class cannot be used in an OSGi context,
 * but it can be accessed from classes that are OSGi compatible if it is only
 * accessed when the runtime context is known not to be OSGi.
 * 
 * @author Joel Håkansson
 */
public class SPIHelper {
	private final static Logger logger = Logger.getLogger(SPIHelper.class.getCanonicalName());
	private static BrailleTranslatorFactoryMakerService translatorFactory;
	private static MarkerProcessorFactoryMakerService markerProcessorFactory;
	private static TextBorderFactoryMakerService textBorderFactory;
	private static ExpressionFactory expressionFactory;
	private static Integer2TextFactoryMakerService integer2TextFactory;
	private static FormatterFactory formatterFactory;
	
	/**
	 * <p>Gets a braille translator factory maker instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the implementation.</p>
	 * @return returns a braille translator factory maker
	 */
	public static BrailleTranslatorFactoryMakerService getBrailleTranslatorFactoryMaker() {
		if (translatorFactory==null) {
			translatorFactory = invokeStatic("org.daisy.dotify.consumer.translator.BrailleTranslatorFactoryMaker", "newInstance");
		}
		return translatorFactory;
	}
	
	/**
	 * <p>Gets a marker processor factory maker instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the implementation.</p>
	 * @return returns a marker processor factory maker
	 */
	public static MarkerProcessorFactoryMakerService getMarkerProcessorFactoryMaker() {
		if (markerProcessorFactory==null) {
			markerProcessorFactory = invokeStatic("org.daisy.dotify.consumer.translator.MarkerProcessorFactoryMaker", "newInstance");
		}
		return markerProcessorFactory;
	}
	
	/**
	 * <p>Gets a text border factory maker instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the implementation.</p>
	 * @return returns a text border factory maker
	 */
	public static TextBorderFactoryMakerService getTextBorderFactoryMaker() {
		if (textBorderFactory==null) {
			textBorderFactory = invokeStatic("org.daisy.dotify.consumer.translator.TextBorderFactoryMaker", "newInstance");
		}
		return textBorderFactory;
	}
	
	/**
	 * <p>Gets an integer 2 text factory maker instance, or null if not found.</p> 
	 * 
	 * <p>Note: This method uses reflexion to get the implementation.</p>
	 * @return returns an integer 2 text factory maker
	 */
	public static Integer2TextFactoryMakerService getInteger2TextFactoryMaker() {
		if (integer2TextFactory==null) {
			integer2TextFactory = invokeStatic("org.daisy.dotify.consumer.text.Integer2TextFactoryMaker", "newInstance");
		}
		return integer2TextFactory;
	}
	
	//the following two differ from the ones above because there isn't an interface for the maker implementations
	//to hide behind and thus they cannot be used, either these should also have interfaces added to the API, or
	//they should be removed

	public static ExpressionFactory getExpressionFactory() {
		if (expressionFactory==null) {
			expressionFactory = ServiceLoader.load(ExpressionFactory.class).iterator().next();
			expressionFactory.setCreatedWithSPI();
		}
		return expressionFactory;
	}
	
	public static FormatterFactory getFormatterFactory() {
		if (formatterFactory==null) {
			formatterFactory = ServiceLoader.load(FormatterFactory.class).iterator().next();
			formatterFactory.setCreatedWithSPI();
		}
		return formatterFactory;
	}

	@SuppressWarnings("unchecked")
	private static <T> T invokeStatic(String clazz, String method) {
		T instance = null;
		try {
			Class<?> cls = Class.forName(clazz);
			Method m = cls.getMethod(method);
			instance = (T)m.invoke(null);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to create instance.", e);
		}
		return instance;
	}

}
