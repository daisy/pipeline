package org.daisy.dotify.api.translator;

/**
 * Provides an exception that indicates a problem configuring a new
 * marker processor instance.
 * 
 * @deprecated This class has been replaced by functionality supplied by {@link TranslatableWithContext}.
 * @author Joel Håkansson
 */
@Deprecated
public class MarkerProcessorConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1320278713114059279L;

	protected MarkerProcessorConfigurationException() {
	}

	protected MarkerProcessorConfigurationException(String message) {
		super(message);
	}

	protected MarkerProcessorConfigurationException(Throwable cause) {
		super(cause);
	}

	protected MarkerProcessorConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
