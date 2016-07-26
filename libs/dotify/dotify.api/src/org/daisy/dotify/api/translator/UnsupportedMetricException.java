package org.daisy.dotify.api.translator;

/**
 * Provides a runtime exception for unsupported metrics.
 * 
 * @author Joel Håkansson
 */
public class UnsupportedMetricException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4173548731907414149L;

	public UnsupportedMetricException() {
	}

	public UnsupportedMetricException(String message) {
		super(message);
	}

	public UnsupportedMetricException(Throwable cause) {
		super(cause);
	}

	public UnsupportedMetricException(String message, Throwable cause) {
		super(message, cause);
	}

}
