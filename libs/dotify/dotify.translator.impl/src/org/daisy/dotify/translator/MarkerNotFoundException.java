package org.daisy.dotify.translator;

public class MarkerNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6301795989858272482L;

	public MarkerNotFoundException() {
	}

	public MarkerNotFoundException(String message) {
		super(message);
	}

	public MarkerNotFoundException(Throwable cause) {
		super(cause);
	}

	public MarkerNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
