package ch.sbs.pipeline.braille.impl;

/*
 * Copied from org.daisy.dotify.impl.text.UndefinedNumberException
 */

public class UndefinedNumberException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6238381557199228181L;

	public UndefinedNumberException() {
		super();
	}

	public UndefinedNumberException(String message, Throwable cause) {
		super(message, cause);
	}

	public UndefinedNumberException(String message) {
		super(message);
	}

	public UndefinedNumberException(Throwable cause) {
		super(cause);
	}

}
