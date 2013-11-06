package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;


/**
 * Provides a proxy for creating a formatter implementation. Objects of this class
 * are detected by the formatter factory and their sole purpose is to create
 * instances of a formatter implementation.
 * @author Joel Håkansson
 */
public interface FormatterFactory {
	
	/**
	 * Creates a new formatter.
	 * @return returns the new formatter.
	 */
	public Formatter newFormatter(String locale, String mode);

	/**
	 * Provides a method to set the BrailleTranslatorFactoryMaker directly. This
	 * is included in the interface as a compromise between OSGi visibility and
	 * SPI compatibility.
	 * 
	 * In an OSGi context, the implementation should not set the implementation
	 * directly, but attach it to the service using DS.
	 * 
	 * @param translatorFactory
	 */
	public void setTranslator(BrailleTranslatorFactoryMakerService translatorFactory);
}