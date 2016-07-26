package org.daisy.dotify.api.formatter;

/**
 * Provides an exception for conditions in a formatter factory configuration.
 * 
 * @author Joel Håkansson
 */
public class FormatterConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2789597556944314161L;

	public FormatterConfigurationException() {
	}

	public FormatterConfigurationException(String message) {
		super(message);
	}

	public FormatterConfigurationException(Throwable cause) {
		super(cause);
	}

	public FormatterConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
