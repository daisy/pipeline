package org.daisy.dotify.impl.input;

/**
 * A ValidatorException is an exception that indicates 
 * conditions in the {@link ValidatorTask} that a reasonable 
 * application might want to catch.
 * 
 * @author Joel Håkansson
 */
public class ValidatorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5418099071057870838L;

	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public ValidatorException() {
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message the detail message
	 */
	public ValidatorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified cause
	 * @param cause the cause
	 */
	public ValidatorException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public ValidatorException(String message, Throwable cause) {
		super(message, cause);
	}

}