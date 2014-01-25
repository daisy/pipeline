package org.daisy.dotify.api.translator;

/**
 * Provides an exception that indicates a problem configuring
 * a new translator instance.
 * 
 * @author Joel Håkansson
 */
public class TranslatorConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1834392372293939932L;

	public TranslatorConfigurationException() {
		super();
	}

	public TranslatorConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TranslatorConfigurationException(String message) {
		super(message);
	}

	public TranslatorConfigurationException(Throwable cause) {
		super(cause);
	}

}
