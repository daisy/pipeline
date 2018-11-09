package org.daisy.dotify.api.text;

/**
 * Provides an exception to indicate problems when configuring an
 * integer to text factory or instance.
 *  
 * @author Joel Håkansson
 *
 */
public class Integer2TextConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3316477128332828379L;

	protected Integer2TextConfigurationException() {
	}

	protected Integer2TextConfigurationException(String message) {
		super(message);
	}

	protected Integer2TextConfigurationException(Throwable cause) {
		super(cause);
	}

	protected Integer2TextConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
