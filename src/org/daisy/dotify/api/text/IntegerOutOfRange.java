package org.daisy.dotify.api.text;

public class IntegerOutOfRange extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3260267525562596727L;

	public IntegerOutOfRange() {
	}

	public IntegerOutOfRange(String message) {
		super(message);
	}

	public IntegerOutOfRange(Throwable cause) {
		super(cause);
	}

	public IntegerOutOfRange(String message, Throwable cause) {
		super(message, cause);
	}

}
