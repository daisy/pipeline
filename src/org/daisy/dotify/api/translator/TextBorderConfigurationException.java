package org.daisy.dotify.api.translator;

/**
 * Provides an exception that indicates a problem configuring 
 * a new text border instance.
 * 
 * @author Joel Håkansson
 */
public class TextBorderConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5930706672695295305L;

	protected TextBorderConfigurationException() {
	}

	protected TextBorderConfigurationException(String message) {
		super(message);
	}

	protected TextBorderConfigurationException(Throwable cause) {
		super(cause);
	}

	protected TextBorderConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
