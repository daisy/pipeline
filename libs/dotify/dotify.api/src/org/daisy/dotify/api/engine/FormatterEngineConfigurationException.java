package org.daisy.dotify.api.engine;

/**
 * Provides an exception for a FormatterEngineFactory
 * 
 * @author Joel Håkansson
 */
public class FormatterEngineConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2413070500611610215L;

	public FormatterEngineConfigurationException() {
		super();
	}

	public FormatterEngineConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FormatterEngineConfigurationException(String message) {
		super(message);
	}

	public FormatterEngineConfigurationException(Throwable cause) {
		super(cause);
	}

}
